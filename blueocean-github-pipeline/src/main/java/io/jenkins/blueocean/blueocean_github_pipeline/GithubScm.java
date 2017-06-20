package io.jenkins.blueocean.blueocean_github_pipeline;

import com.cloudbees.plugins.credentials.CredentialsScope;
import com.cloudbees.plugins.credentials.common.StandardUsernamePasswordCredentials;
import com.cloudbees.plugins.credentials.domains.DomainSpecification;
import com.cloudbees.plugins.credentials.impl.UsernamePasswordCredentialsImpl;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.introspect.VisibilityChecker;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import hudson.Extension;
import hudson.model.User;
import hudson.tasks.Mailer;
import io.jenkins.blueocean.commons.ErrorMessage;
import io.jenkins.blueocean.commons.JsonConverter;
import io.jenkins.blueocean.commons.ServiceException;
import io.jenkins.blueocean.credential.CredentialsUtils;
import io.jenkins.blueocean.rest.Reachable;
import io.jenkins.blueocean.rest.hal.Link;
import io.jenkins.blueocean.rest.impl.pipeline.credential.BlueOceanDomainRequirement;
import io.jenkins.blueocean.rest.impl.pipeline.credential.BlueOceanDomainSpecification;
import io.jenkins.blueocean.rest.impl.pipeline.scm.Scm;
import io.jenkins.blueocean.rest.impl.pipeline.scm.ScmFactory;
import io.jenkins.blueocean.rest.impl.pipeline.scm.ScmOrganization;
import io.jenkins.blueocean.rest.model.Container;
import net.sf.json.JSONObject;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.jenkinsci.plugins.github_branch_source.GitHubSCMSource;
import org.kohsuke.github.GHMyself;
import org.kohsuke.github.GHOrganization;
import org.kohsuke.github.GHUser;
import org.kohsuke.github.GitHub;
import org.kohsuke.github.GitHubBuilder;
import org.kohsuke.github.HttpException;
import org.kohsuke.github.RateLimitHandler;
import org.kohsuke.stapler.HttpResponse;
import org.kohsuke.stapler.Stapler;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;
import org.kohsuke.stapler.json.JsonBody;

import javax.annotation.Nonnull;
import javax.servlet.ServletException;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.ANY;
import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;

/**
 * @author Vivek Pandey
 */
public class GithubScm extends Scm {
    //Used by tests to mock github
    private static final String ID = "github";

    //desired scopes
    private static final String USER_EMAIL_SCOPE = "user:email";
    private static final String USER_SCOPE = "user";
    private static final String REPO_SCOPE = "repo";
    static final String DOMAIN_NAME="blueocean-github-domain";

    private final Link self;

    static final ObjectMapper om = new ObjectMapper();
    static {
        om.setVisibilityChecker(new VisibilityChecker.Std(NONE, NONE, NONE, NONE, ANY));
        om.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

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
        StaplerRequest request = Stapler.getCurrentRequest();
        Preconditions.checkNotNull(request, "Must be called in HTTP request context");
        String endpointUri = request.getParameter("apiUrl");
        return (endpointUri != null) ? endpointUri : GitHubSCMSource.GITHUB_URL;
    }

    public String getCredentialDomainName(){
        return DOMAIN_NAME;
    }

    @Override
    public String getCredentialId(){
        StandardUsernamePasswordCredentials githubCredential = CredentialsUtils.findCredential(getId(), StandardUsernamePasswordCredentials.class, new BlueOceanDomainRequirement());
        if(githubCredential != null){
            return githubCredential.getId();
        }
        return null;
    }

    @Override
    public Container<ScmOrganization> getOrganizations() {
        StaplerRequest request = Stapler.getCurrentRequest();

        String credentialId = getCredentialIdFromRequest(request);

        User authenticatedUser = getAuthenticatedUser();
        final StandardUsernamePasswordCredentials credential = CredentialsUtils.findCredential(credentialId, StandardUsernamePasswordCredentials.class, new BlueOceanDomainRequirement());

        if(credential == null){
            throw new ServiceException.BadRequestException(String.format("Credential id: %s not found for user %s", credentialId, authenticatedUser.getId()));
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
            if(e instanceof HttpException) {
                HttpException ex = (HttpException) e;
                if (ex.getResponseCode() == 401) {
                    throw new ServiceException
                            .PreconditionRequired("Invalid Github accessToken", ex);
                }else if(ex.getResponseCode() == 403){
                    throw new ServiceException
                            .PreconditionRequired("Github accessToken does not have required scopes. Expected scopes 'user:email, repo'", ex);
                }
            }
            throw new ServiceException.UnexpectedErrorException(e.getMessage(), e);
        }
    }

     private static String getCredentialIdFromRequest(StaplerRequest request){
        String credentialId = request.getParameter(CREDENTIAL_ID);

        if(credentialId == null){
            credentialId = request.getHeader(X_CREDENTIAL_ID);
        }
        if(credentialId == null){
            throw new ServiceException.BadRequestException("Missing credential id. It must be provided either as HTTP header: " + X_CREDENTIAL_ID+" or as query parameter 'credentialId'");
        }
        return credentialId;
    }

    static class RateLimitHandlerImpl extends RateLimitHandler{
        @Override
        public void onError(IOException e, HttpURLConnection httpURLConnection) throws IOException {
            throw new ServiceException.BadRequestException("API rate limit reached."+e.getMessage(), e);
        }
    }

    @Override
    public HttpResponse validateAndCreate(@JsonBody JSONObject request) {
        String accessToken = (String) request.get("accessToken");
        if(accessToken == null){
            throw new ServiceException.BadRequestException("accessToken is required");
        }
        try {
            User authenticatedUser =  getAuthenticatedUser();

            HttpURLConnection connection = connect(String.format("%s/%s", getUri(), "user"),accessToken);
            validateAccessTokenScopes(connection);
            String data = IOUtils.toString(connection.getInputStream());
            GHUser user = GithubScm.om.readValue(data, GHUser.class);

            if(user.getEmail() != null){
                Mailer.UserProperty p = authenticatedUser.getProperty(Mailer.UserProperty.class);
                //XXX: If there is already email address of this user, should we update it with
                // the one from Github?
                if (p==null){
                    authenticatedUser.addProperty(new Mailer.UserProperty(user.getEmail()));
                }
            }

            //Now we know the token is valid. Lets find credential
            StandardUsernamePasswordCredentials githubCredential = CredentialsUtils.findCredential(getId(), StandardUsernamePasswordCredentials.class, new BlueOceanDomainRequirement());

            final StandardUsernamePasswordCredentials credential = new UsernamePasswordCredentialsImpl(CredentialsScope.USER, "github", "Github Access Token", authenticatedUser.getId(), accessToken);


            if(githubCredential == null) {
                CredentialsUtils.createCredentialsInUserStore(
                        credential, authenticatedUser, getCredentialsDomainName(getUri()),
                        ImmutableList.<DomainSpecification>of(new BlueOceanDomainSpecification()));
            }else{
                CredentialsUtils.updateCredentialsInUserStore(
                        githubCredential, credential, authenticatedUser, getCredentialsDomainName(getUri()),
                        ImmutableList.<DomainSpecification>of(new BlueOceanDomainSpecification()));
            }

            return createResponse(credential.getId());

        } catch (IOException e) {
            throw new ServiceException.UnexpectedErrorException(e.getMessage());
        }
    }

    static HttpURLConnection connect(String apiUrl, String accessToken) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) new URL(apiUrl).openConnection();

        connection.setDoOutput(true);
        connection.setRequestMethod("GET");
        connection.setRequestProperty("Content-type", "application/json");
        connection.setRequestProperty("Authorization", "token "+accessToken);
        connection.connect();

        int status = connection.getResponseCode();
        if(status == 401){
            throw new ServiceException.PreconditionRequired("Invalid accessToken");
        }
        if(status == 403){
            throw new ServiceException.PreconditionRequired("Github accessToken does not have required scopes. Expected scopes 'user:email, repo'");
        }
        if(status == 404){
            throw new ServiceException.NotFoundException("Not Found");
        }
        if(status != 200) {
            throw new ServiceException.BadRequestException(String.format("Github Api returned error: %s. Error message: %s.", connection.getResponseCode(), connection.getResponseMessage()));
        }

        return connection;
    }

    static void validateAccessTokenScopes(HttpURLConnection connection) {
        //check for user:email or user AND repo scopes
        String scopesHeader = connection.getHeaderField("X-OAuth-Scopes");
        if(scopesHeader == null){
            throw new ServiceException.PreconditionRequired("No scopes associated with this token. Expected scopes 'user:email, repo'.");
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
            throw new ServiceException.PreconditionRequired("Invalid token, its missing scopes: "+ StringUtils.join(missingScopes, ","));
        }
    }

    private HttpResponse createResponse(final String credentialId) {
        return new HttpResponse() {
            @Override
            public void generateResponse(StaplerRequest req, StaplerResponse rsp, Object node) throws IOException, ServletException {
                rsp.setStatus(200);
                rsp.getWriter().print(JsonConverter.toJson(ImmutableMap.of("credentialId", credentialId)));
            }
        };
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

    static User getAuthenticatedUser(){
        User authenticatedUser = User.current();
        if(authenticatedUser == null){
            throw new ServiceException.UnauthorizedException("No logged in user found");
        }
        return authenticatedUser;
    }

    private String getCredentialsDomainName(String apiUri) {
        java.net.URI uri;
        try {
            uri = new URI(apiUri);
        } catch (URISyntaxException e) {
            throw new ServiceException.UnexpectedErrorException(new ErrorMessage(400, "Invalid URI: "+apiUri));
        }
        String domainName = getCredentialDomainName();
        if(this instanceof GithubEnterpriseScm){
            return domainName + "-" + uri.getHost();
        }
        return domainName;
    }
}
