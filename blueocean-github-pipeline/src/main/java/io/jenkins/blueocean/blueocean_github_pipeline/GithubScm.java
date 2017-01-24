package io.jenkins.blueocean.blueocean_github_pipeline;

import com.cloudbees.plugins.credentials.CredentialsMatchers;
import com.cloudbees.plugins.credentials.CredentialsProvider;
import com.cloudbees.plugins.credentials.CredentialsScope;
import com.cloudbees.plugins.credentials.CredentialsStore;
import com.cloudbees.plugins.credentials.common.StandardUsernamePasswordCredentials;
import com.cloudbees.plugins.credentials.domains.Domain;
import com.cloudbees.plugins.credentials.domains.DomainRequirement;
import com.cloudbees.plugins.credentials.domains.DomainSpecification;
import com.cloudbees.plugins.credentials.domains.HostnamePortSpecification;
import com.cloudbees.plugins.credentials.domains.HostnameSpecification;
import com.cloudbees.plugins.credentials.domains.PathSpecification;
import com.cloudbees.plugins.credentials.domains.SchemeSpecification;
import com.cloudbees.plugins.credentials.domains.URIRequirementBuilder;
import com.cloudbees.plugins.credentials.impl.UsernamePasswordCredentialsImpl;
import com.google.common.collect.ImmutableMap;
import hudson.Extension;
import hudson.model.User;
import hudson.security.ACL;
import hudson.tasks.Mailer;
import io.jenkins.blueocean.commons.JsonConverter;
import io.jenkins.blueocean.commons.ServiceException;
import io.jenkins.blueocean.rest.Reachable;
import io.jenkins.blueocean.rest.hal.Link;
import io.jenkins.blueocean.rest.impl.pipeline.scm.Scm;
import io.jenkins.blueocean.rest.impl.pipeline.scm.ScmFactory;
import io.jenkins.blueocean.rest.impl.pipeline.scm.ScmOrganization;
import io.jenkins.blueocean.rest.model.Container;
import jenkins.model.Jenkins;
import net.sf.json.JSONObject;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.kohsuke.github.GHMyself;
import org.kohsuke.github.GHOrganization;
import org.kohsuke.github.GHUser;
import org.kohsuke.github.GitHub;
import org.kohsuke.github.GitHubBuilder;
import org.kohsuke.github.HttpConnector;
import org.kohsuke.github.RateLimitHandler;
import org.kohsuke.stapler.HttpResponse;
import org.kohsuke.stapler.Stapler;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;
import org.kohsuke.stapler.json.JsonBody;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.servlet.ServletException;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;

/**
 * @author Vivek Pandey
 */
public class GithubScm extends Scm {
    static final String DEFAULT_API_URI = "https://api.github.com";
    private static final String ID = "github";

    //desired scopes
    private static final String USER_EMAIL_SCOPE = "user:email";
    private static final String USER_SCOPE = "user";
    private static final String REPO_SCOPE = "repo";
    private static final String DOMAIN_NAME="github-domain";

    private final Link self;

    public GithubScm(Reachable parent) {
        this.self = parent.getLink().rel("github");
    }

    @Override
    public Link getLink() {
        return self;
    }

    @Override
    public @Nonnull String getId() {
        return ID;
    }

    @Override
    public @Nonnull String getUri() {
        return DEFAULT_API_URI;
    }


    @Override
    public String getCredentialId(){
        User authenticatedUser =  User.current();
        if(authenticatedUser == null){
            throw new ServiceException.UnauthorizedException("No authenticated user found");
        }
        StandardUsernamePasswordCredentials githubCredential = findUsernamePasswordCredential(getUri(), getGithubCredentialId(authenticatedUser));
        if(githubCredential != null){
            return githubCredential.getId();
        }
        return null;
    }

    @Override
    public Container<ScmOrganization> getOrganizations() {
        StaplerRequest request = Stapler.getCurrentRequest();

        String credentialId = getCredentialIdFromRequest(request);

        final StandardUsernamePasswordCredentials credential = GithubScm.findUsernamePasswordCredential(getUri(),credentialId);

        if(credential == null){
            String user = User.current() == null ? "anonymous" : User.current().getId();
            throw new ServiceException.BadRequestExpception(String.format("Credential id: %s not found for user %s", credentialId, user));
        }

        String accessToken = credential.getPassword().getPlainText();

        try {
            GitHub github = new GitHubBuilder().withOAuthToken(accessToken)
                    .withRateLimitHandler(new RateLimitHandlerImpl())
                    .withEndpoint(getUri()).build();

            final Link link = getLink().rel("organizations");

            Map<String, ScmOrganization> orgMap = new LinkedHashMap<>(); // preserve the same order that github org api returns

            for(Map.Entry<String, GHOrganization> entry: github.getMyOrganizations().entrySet()){
                    orgMap.put(entry.getKey(),
                            new GithubOrganization(GithubScm.this, entry.getValue(), credential, link));
            }

            GHMyself user = github.getMyself();
            if(orgMap.get(user.getLogin()) == null){ //this is to take care of case if/when github starts reporting user login as org later on
                orgMap = new HashMap<>(orgMap);
                orgMap.put(user.getLogin(), new GithubUserOrganization(user, credential, this));
            }
            final Map<String, ScmOrganization> orgs = orgMap;
            return new Container<ScmOrganization>() {
                @Override
                public ScmOrganization get(String name) {
                    ScmOrganization org = orgs.get(name);
                    if(org == null){
                        throw new ServiceException.NotFoundException(String.format("GitHub organization %s not found", name));
                    }
                    return org;
                }

                @Override
                public Link getLink() {
                    return link;
                }

                @Override
                public Iterator<ScmOrganization> iterator() {
                    return orgs.values().iterator();
                }
            };
        } catch (IOException e) {
            throw new ServiceException.UnexpectedErrorException(e.getMessage(), e);
        }
    }

     private static String getCredentialIdFromRequest(StaplerRequest request){
        String credentialId = request.getParameter(CREDENTIAL_ID);

        if(credentialId == null){
            credentialId = request.getHeader(X_CREDENTIAL_ID);
        }
        if(credentialId == null){
            throw new ServiceException.BadRequestExpception("Missing credential id. It must be provided either as HTTP header: " + X_CREDENTIAL_ID+" or as query parameter 'credentialId'");
        }
        return credentialId;
    }

    public static class RateLimitHandlerImpl extends RateLimitHandler{
        @Override
        public void onError(IOException e, HttpURLConnection httpURLConnection) throws IOException {
            throw new ServiceException.BadRequestExpception("API rate limit reached."+e.getMessage(), e);
        }
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

            HttpURLConnection connection = connect(String.format("%s/%s", getUri(), "user"),accessToken);

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

            GHUser user = getResponse(connection, GHUser.class);

            if(user.getEmail() != null){
                Mailer.UserProperty p = authenticatedUser.getProperty(Mailer.UserProperty.class);
                //XXX: If there is already email address of this user, should we update it with
                // the one from Github?
                if (p==null){
                    authenticatedUser.addProperty(new Mailer.UserProperty(user.getEmail()));
                }
            }


            String expectedCredId = getGithubCredentialId(authenticatedUser);
            //Now we know the token is valid. Lets find credential
            StandardUsernamePasswordCredentials githubCredential = findUsernamePasswordCredential(getUri(), expectedCredId);

            final StandardUsernamePasswordCredentials credential = new UsernamePasswordCredentialsImpl(CredentialsScope.GLOBAL,
                    expectedCredId, "Github Access Token", user.getLogin(), accessToken);


            CredentialsStore store=null;
            for(CredentialsStore s: CredentialsProvider.lookupStores(Jenkins.getInstance())){
                if(s.hasPermission(CredentialsProvider.CREATE) && s.hasPermission(CredentialsProvider.UPDATE)){
                    store = s;
                    break;
                }
            }

            if(store == null){
                throw new ServiceException.ForbiddenException(String.format("Logged in user: %s doesn't have writable credentials store", authenticatedUser.getId()));
            }

            Domain domain = store.getDomainByName(DOMAIN_NAME);
            if(domain == null){
                java.net.URI uri = new URI(getUri());

                List<DomainSpecification> domainSpecifications = new ArrayList<>();

                // XXX: UriRequirementBuilder.fromUri() maps "" path to "/", so need to take care of it here
                String path = uri.getRawPath() == null ? null : (uri.getRawPath().trim().isEmpty() ? "/" : uri.getRawPath());
                domainSpecifications.add(new PathSpecification(path, "", false));
                if(uri.getPort() != -1){
                    domainSpecifications.add(new HostnamePortSpecification(uri.getHost()+":"+uri.getPort(), null));
                }else{
                    domainSpecifications.add(new HostnameSpecification(uri.getHost(),null));
                }
                domainSpecifications.add(new SchemeSpecification(uri.getScheme()));

                boolean result = store.addDomain(new Domain(DOMAIN_NAME,
                        "Github Domain to store personal access token",
                        domainSpecifications
                ));
                if(!result){
                    throw new ServiceException.BadRequestExpception("Github accessToken is valid but no valid credential domain found and could not be created");
                }
                domain = store.getDomainByName(DOMAIN_NAME);
                if(domain == null){
                    throw new ServiceException.BadRequestExpception("Github accessToken is valid but no valid credential domain found and could not be created");
                }
            }

            if(githubCredential == null){
                if(!store.addCredentials(domain, credential)){
                    throw new ServiceException.UnexpectedErrorException("Failed to add credential to domain");
                }

            }else{
                if(!store.updateCredentials(domain, githubCredential, credential)){
                    throw new ServiceException.UnexpectedErrorException("Failed to update credential to domain");
                }
            }
            return createResponse(credential.getId());

        } catch (IOException | URISyntaxException e) {
            throw new ServiceException.UnexpectedErrorException(e.getMessage());
        }
    }

    private static String getGithubCredentialId(User user){
        return String.format("github-%s", user.getId());
    }

    static <T> T getResponse(HttpURLConnection connection, Class<T> type) throws IOException {
         InputStream in = null;
         try {
            in = wrapStream(connection, connection.getInputStream());
            String data = IOUtils.toString(in);
            return JsonConverter.toJava(data, type);
        }finally {
            IOUtils.closeQuietly(in);
        }
    }

    static HttpURLConnection connect(String apiUrl, String accessToken) throws IOException {
        HttpURLConnection connection = HttpConnector.DEFAULT.connect(new URL(apiUrl));

        connection.setDoOutput(true);
        connection.setRequestMethod("GET");
        connection.setRequestProperty("Content-type", "application/json");
        connection.setRequestProperty("Authorization", "token "+accessToken);
        connection.connect();

        int status = connection.getResponseCode();
        if(status == 401 || status == 403){
            throw new ServiceException.ForbiddenException("Invalid accessToken");
        }
        if(status == 404){
            throw new ServiceException.NotFoundException("Not Found");
        }
        if(status != 200) {
            throw new ServiceException.BadRequestExpception(String.format("Github Api returned error: %s. Error message: %s.", connection.getResponseCode(), connection.getResponseMessage()));
        }

        return connection;
    }

    /**
     * Handles the "Content-Encoding" header.
     */
    private static InputStream wrapStream(HttpURLConnection connection, InputStream in) throws IOException {
        String encoding = connection.getContentEncoding();
        if (encoding==null || in==null) return in;
        if (encoding.equals("gzip"))    return new GZIPInputStream(in);

        throw new UnsupportedOperationException("Unexpected Content-Encoding: "+encoding);
    }

    private Domain getFirstDomain(CredentialsStore store){
        for(Domain d:store.getDomains()){
            if(d.getName() != null){
                return d;
            }
        }
        return null;
    }

     private HttpResponse createResponse(final String credentialId){
        return new HttpResponse() {
            @Override
            public void generateResponse(StaplerRequest req, StaplerResponse rsp, Object node) throws IOException, ServletException {
                rsp.setStatus(200);
                rsp.getWriter().print(JsonConverter.toJson(ImmutableMap.of("credentialId", credentialId)));
            }
        };
    }

     private static StandardUsernamePasswordCredentials findUsernamePasswordCredential(String uri, String credentialId){
        return CredentialsMatchers.firstOrNull(
                CredentialsProvider.lookupCredentials(
                        StandardUsernamePasswordCredentials.class,
                        Jenkins.getInstance(),
                        ACL.SYSTEM,
                        URIRequirementBuilder.fromUri(uri).build()),
                CredentialsMatchers.allOf(CredentialsMatchers.withId(credentialId),
                        CredentialsMatchers.anyOf(CredentialsMatchers.instanceOf(StandardUsernamePasswordCredentials.class)))
        );
    }

    private static @CheckForNull StandardUsernamePasswordCredentials findUsernamePasswordCredential(String id){
        if(User.current() == null){
            throw new ServiceException.UnauthorizedException("No authenticated user found. Please login");
        }
        return CredentialsMatchers.firstOrNull(
                CredentialsProvider.lookupCredentials(
                        StandardUsernamePasswordCredentials.class,
                        Jenkins.getInstance(),
                        ACL.SYSTEM,
                        Collections.<DomainRequirement>emptyList()),
                CredentialsMatchers.allOf(CredentialsMatchers.withId(id),
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
