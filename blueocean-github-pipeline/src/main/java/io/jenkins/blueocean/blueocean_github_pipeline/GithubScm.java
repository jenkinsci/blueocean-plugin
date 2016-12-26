package io.jenkins.blueocean.blueocean_github_pipeline;

import com.cloudbees.plugins.credentials.CredentialsMatchers;
import com.cloudbees.plugins.credentials.CredentialsProvider;
import com.cloudbees.plugins.credentials.CredentialsScope;
import com.cloudbees.plugins.credentials.CredentialsStore;
import com.cloudbees.plugins.credentials.common.StandardUsernamePasswordCredentials;
import com.cloudbees.plugins.credentials.domains.Domain;
import com.cloudbees.plugins.credentials.domains.URIRequirementBuilder;
import com.cloudbees.plugins.credentials.impl.UsernamePasswordCredentialsImpl;
import com.google.common.collect.ImmutableMap;
import hudson.Extension;
import hudson.model.User;
import hudson.tasks.Mailer;
import io.jenkins.blueocean.commons.JsonConverter;
import io.jenkins.blueocean.commons.ServiceException;
import io.jenkins.blueocean.rest.Reachable;
import io.jenkins.blueocean.rest.hal.Link;
import io.jenkins.blueocean.rest.impl.pipeline.scm.Scm;
import io.jenkins.blueocean.rest.impl.pipeline.scm.ScmFactory;
import jenkins.model.Jenkins;
import net.sf.json.JSONObject;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.kohsuke.github.GHUser;
import org.kohsuke.github.HttpConnector;
import org.kohsuke.stapler.HttpResponse;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;
import org.kohsuke.stapler.json.JsonBody;

import javax.annotation.Nonnull;
import javax.servlet.ServletException;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Vivek Pandey
 */
public class GithubScm extends Scm {
    private static final String DEFAULT_API_URI = "https://api.github.com";
    private static final String ID = "github";

    //desired scopes
    private static final String USER_EMAIL_SCOPE = "user:email";
    private static final String USER_SCOPE = "user";
    private static final String REPO_SCOPE = "repo";

    private final Link self;

    public GithubScm(Reachable parent) {
        this.self = parent.getLink().rel("github");
    }

    @Override
    public Link getLink() {
        return self;
    }

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public String getUri() {
        return DEFAULT_API_URI;
    }


    @Override
    public String getCredentialId(){
        StandardUsernamePasswordCredentials githubCredential = findUsernamePasswordCredential(this);
        if(githubCredential != null){
            return githubCredential.getId();
        }
        return null;
    }

    @Override
    public HttpResponse validateAndCreate(@JsonBody JSONObject request) {
        String accessToken = (String) request.get("accessToken");
        if(accessToken == null){
            throw new ServiceException.BadRequestExpception("accessToken is required");
        }
        try {
            User authenticatedUser =  User.current();
            if(authenticatedUser == null){
                throw new ServiceException.UnauthorizedException("No authenticated user found");
            }

            HttpURLConnection connection = HttpConnector.DEFAULT.connect(new URL(getUri()+"/user"));

            connection.setDoOutput(true);
            connection.setRequestProperty("Content-type", "application/json");
            connection.setRequestProperty("Authorization", "token "+accessToken);
            connection.connect();

            int status = connection.getResponseCode();
            if(status == 401 || status == 403){
                throw new ServiceException.ForbiddenException("Invalid accessToken");
            }
            if(status != 200) {
                throw new ServiceException.BadRequestExpception(String.format("Github Api returned error: %s. Error message: %s.", connection.getResponseCode(), connection.getResponseMessage()));
            }
            //check for user:email or user AND repo scopes
            String scopesHeader = connection.getHeaderField("X-OAuth-Scopes");
            if(scopesHeader == null){
                throw new ServiceException.ForbiddenException("No scopes associated with this token. Expected scopes 'user:email, repo'.");
            }
            List<String> scopes = new ArrayList<>();
            for(String s: scopesHeader.split(",")){
                scopes.add(s.trim());
            }
            List<String> missingScopes = new ArrayList<>();
            if(!scopes.contains(USER_EMAIL_SCOPE) && !scopes.contains(USER_SCOPE)){
                missingScopes.add(USER_EMAIL_SCOPE);
            }
            if(!scopes.contains(REPO_SCOPE)){
                missingScopes.add(REPO_SCOPE);
            }
            if(!missingScopes.isEmpty()){
                throw new ServiceException.ForbiddenException("Invalid token, its missing scopes: "+ StringUtils.join(missingScopes, ","));
            }

            String data = IOUtils.toString(connection.getInputStream());
            GHUser user = JsonConverter.toJava(data, GHUser.class);

            if(user.getEmail() != null){
                Mailer.UserProperty p = authenticatedUser.getProperty(Mailer.UserProperty.class);
                //XXX: If there is already email address of this user, should we update it with
                // the one from Github?
                if (p==null){
                    authenticatedUser.addProperty(new Mailer.UserProperty(user.getEmail()));
                }
            }


            //Now we know the token is valid. Lets find credential
            StandardUsernamePasswordCredentials githubCredential = findUsernamePasswordCredential(this);

            final StandardUsernamePasswordCredentials credential = new UsernamePasswordCredentialsImpl(CredentialsScope.USER, "github", "Github Access Token", user.getLogin(), accessToken);


            CredentialsStore store=null;
            for(CredentialsStore s: CredentialsProvider.lookupStores(authenticatedUser)){
                if(s.hasPermission(CredentialsProvider.CREATE) && s.hasPermission(CredentialsProvider.UPDATE)){
                    store = s;
                    break;
                }
            }

            if(store == null){
                throw new ServiceException.ForbiddenException(String.format("Logged in user: %s doesn't have writable credentials store", authenticatedUser.getId()));
            }

            Domain domain = getFirstDomain(store);
            if(domain == null){
                throw new ServiceException.BadRequestExpception("Github accessToken is valid but no valid credential domain found");
            }
            if(githubCredential == null){
                store.addCredentials(domain, credential);
            }else{
                store.updateCredentials(domain, githubCredential, credential);
            }
            return createResponse(credential.getId());

        } catch (IOException e) {
            throw new ServiceException.UnexpectedErrorException(e.getMessage());
        }
    }

    public Domain getFirstDomain(CredentialsStore store){
        for(Domain d:store.getDomains()){
            if(d.getName() != null){
                return d;
            }
        }
        return null;
    }

    public static HttpResponse createResponse(final String credentialId){
        return new HttpResponse() {
            @Override
            public void generateResponse(StaplerRequest req, StaplerResponse rsp, Object node) throws IOException, ServletException {
                rsp.setStatus(200);
                rsp.getWriter().print(JsonConverter.toJson(ImmutableMap.of("credentialId", credentialId)));
            }
        };
    }

    public static StandardUsernamePasswordCredentials findUsernamePasswordCredential(Scm scm){
        return CredentialsMatchers.firstOrNull(
                CredentialsProvider.lookupCredentials(
                        StandardUsernamePasswordCredentials.class,
                        Jenkins.getInstance(),
                        Jenkins.getAuthentication(),
                        URIRequirementBuilder.fromUri(scm.getUri()).build()),
                CredentialsMatchers.allOf(CredentialsMatchers.withId(scm.getId()),
                        CredentialsMatchers.anyOf(CredentialsMatchers.instanceOf(StandardUsernamePasswordCredentials.class)))
        );
    }

    @Extension
    public static class GithubScmFactory extends ScmFactory {
        @Override
        public Scm getScm(@Nonnull String id, @Nonnull Reachable parent) {
            if(id.equals(ID)){
                return new GithubScm(parent);
            }
            return null;
        }

        @Nonnull
        @Override
        public Scm getScm(Reachable parent) {
            return new GithubScm(parent);
        }
    }
}
