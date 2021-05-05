package io.jenkins.blueocean.blueocean_github_pipeline;

import com.cloudbees.plugins.credentials.CredentialsScope;
import com.cloudbees.plugins.credentials.common.StandardUsernamePasswordCredentials;
import com.cloudbees.plugins.credentials.impl.UsernamePasswordCredentialsImpl;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.google.common.base.Preconditions;
import hudson.Extension;
import hudson.model.Item;
import hudson.model.User;
import hudson.tasks.Mailer;
import io.jenkins.blueocean.commons.ErrorMessage;
import io.jenkins.blueocean.commons.ServiceException;
import io.jenkins.blueocean.commons.stapler.TreeResponse;
import io.jenkins.blueocean.credential.CredentialsUtils;
import io.jenkins.blueocean.rest.Reachable;
import io.jenkins.blueocean.rest.hal.Link;
import io.jenkins.blueocean.rest.impl.pipeline.credential.BlueOceanDomainRequirement;
import io.jenkins.blueocean.rest.impl.pipeline.credential.BlueOceanDomainSpecification;
import io.jenkins.blueocean.rest.impl.pipeline.scm.AbstractScm;
import io.jenkins.blueocean.rest.impl.pipeline.scm.Scm;
import io.jenkins.blueocean.rest.impl.pipeline.scm.ScmFactory;
import io.jenkins.blueocean.rest.impl.pipeline.scm.ScmOrganization;
import io.jenkins.blueocean.rest.impl.pipeline.scm.ScmServerEndpointContainer;
import io.jenkins.blueocean.rest.model.Container;
import jenkins.model.Jenkins;
import net.sf.json.JSONObject;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.jenkinsci.plugins.github_branch_source.GitHubSCMSource;
import org.jenkinsci.plugins.workflow.multibranch.WorkflowMultiBranchProject;
import org.kohsuke.github.GHMyself;
import org.kohsuke.github.GHOrganization;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GHUser;
import org.kohsuke.github.GitHub;
import org.kohsuke.github.HttpException;
import org.kohsuke.stapler.HttpResponse;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.Stapler;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.WebMethod;
import org.kohsuke.stapler.json.JsonBody;
import org.kohsuke.stapler.verb.GET;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Vivek Pandey
 */
public class GithubScm extends AbstractScm {
    //Used by tests to mock github
    public static final String ID = "github";

    //desired scopes
    private static final String USER_EMAIL_SCOPE = "user:email";
    private static final String USER_SCOPE = "user";
    private static final String REPO_SCOPE = "repo";
    static final String DOMAIN_NAME="blueocean-github-domain";
    static final String CREDENTIAL_DESCRIPTION = "GitHub Access Token";


    protected final Reachable parent;

    @Nonnull
    static ObjectWriter getMappingObjectWriter() {
        return GitHub.getMappingObjectWriter();
    }

    @Nonnull
    static ObjectReader getMappingObjectReader() {
        return GitHub.getMappingObjectReader();
    }

    public GithubScm(Reachable parent) {
        this.parent = parent;
    }

    @Override
    public Link getLink() {
        return parent.getLink().rel("github");
    }

    @Override
    public @Nonnull String getId() {
        return ID;
    }

    @Override
    public @Nonnull String getUri() {
        String apiUri = getCustomApiUri();

        // NOTE: GithubScm only uses a custom apiUri in the context of automated tests
        if (!StringUtils.isEmpty(apiUri)) {
            return apiUri;
        }

        return GitHubSCMSource.GITHUB_URL;
    }

    public String getCredentialDomainName(){
        return DOMAIN_NAME;
    }

    @Override
    public String getCredentialId(){
        StandardUsernamePasswordCredentials githubCredential = getCredential(getUri());
        if(githubCredential != null){
            return githubCredential.getId();
        }
        return null;
    }

    StandardUsernamePasswordCredentials getCredential(String apiUrl){
        String credentialId = createCredentialId(apiUrl);
        return CredentialsUtils.findCredential(credentialId,
                StandardUsernamePasswordCredentials.class,
                new BlueOceanDomainRequirement());
    }

    @Override
    public Object getState() {
        this.validateExistingAccessToken();
        return super.getState();
    }

    private StandardUsernamePasswordCredentials getCredential(){
        StaplerRequest request = Stapler.getCurrentRequest();
        String credentialId = GithubCredentialUtils.computeCredentialId(getCredentialIdFromRequest(request), getId(), getUri());
        User authenticatedUser = getAuthenticatedUser();
        final StandardUsernamePasswordCredentials credential = CredentialsUtils.findCredential(credentialId, StandardUsernamePasswordCredentials.class, new BlueOceanDomainRequirement());

        if(credential == null){
            throw new ServiceException.BadRequestException(String.format("Credential id: %s not found for user %s", credentialId, authenticatedUser.getId()));
        }
        return credential;
    }

    /**
     *
     * @param jobName the job name
     * @param apiUrl github api url
     * @return GHRepository used by the job
     */
    @GET
    @WebMethod(name = "repository")
    @TreeResponse
    public GithubRepository getRepository(@QueryParameter String jobName, @QueryParameter String apiUrl) {
        Item item = Jenkins.get().getItem( jobName);
        if (item == null){
            throw new ServiceException.NotFoundException(String.format("Job %s not found", jobName));
        }
        GitHubSCMSource gitHubSCMSource = ((GitHubSCMSource)((WorkflowMultiBranchProject)item).getSCMSource( "blueocean"));
        if(gitHubSCMSource==null){
            throw new ServiceException.NotFoundException(String.format("GitHubSCMSource for Job %s not found", jobName));
        }
        String repoOwner = gitHubSCMSource.getRepoOwner();
        String repoName = gitHubSCMSource.getRepository();

        StandardUsernamePasswordCredentials credential = getCredential();
        String accessToken = credential.getPassword().getPlainText();

        try {
            String url = String.format("%s/repos/%s/%s", apiUrl, repoOwner, repoName);
            GHRepository ghRepository = HttpRequest.get(url).withAuthorizationToken(accessToken).to(GHRepository.class);
            return new GithubRepository(ghRepository, credential, this);
        } catch (IOException e) {
            throw new ServiceException.UnexpectedErrorException(e.getMessage(),e);
        }
    }

    @Override
    public Container<ScmOrganization> getOrganizations() {
        StandardUsernamePasswordCredentials credential = getCredential();
        String accessToken = credential.getPassword().getPlainText();

        try {
            GitHub github = GitHubFactory.connect(accessToken, getUri());

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
                            .PreconditionRequired("Invalid GitHub accessToken", ex);
                }else if(ex.getResponseCode() == 403){
                    throw new ServiceException
                            .PreconditionRequired("GitHub accessToken does not have required scopes. Expected scopes 'user:email, repo'", ex);
                }
            }
            throw new ServiceException.UnexpectedErrorException(e.getMessage(), e);
        }
    }

    @Override
    public ScmServerEndpointContainer getServers() {
        return null;
    }

    public boolean isOrganizationAvatarSupported() {
        return true;
    }

    protected @Nonnull String createCredentialId(@Nonnull String apiUrl) {
        return GithubCredentialUtils.computeCredentialId(null, GithubScm.ID, apiUrl);
    }

    protected @Nonnull String getCredentialDescription() {
        return CREDENTIAL_DESCRIPTION;
    }

    protected @Nonnull String getCustomApiUri() {
        StaplerRequest request = Stapler.getCurrentRequest();
        Preconditions.checkNotNull(request, "Must be called in HTTP request context");
        String apiUri = request.getParameter("apiUrl");

        // if "apiUrl" parameter was supplied, parse and trim trailing slash
        if (!StringUtils.isEmpty(apiUri)) {
            try {
                new URI(apiUri);
            } catch (URISyntaxException ex) {
                throw new ServiceException.BadRequestException(new ErrorMessage(400, "Invalid URI: " + apiUri));
            }
            apiUri = normalizeUrl(apiUri);
        } else {
            apiUri = "";
        }

        return apiUri;
    }

    static String normalizeUrl(@Nonnull String apiUrl){
        if (apiUrl.endsWith("/")) {
            apiUrl = apiUrl.substring(0, apiUrl.length() - 1);
        }
        return apiUrl;
    }

    @Override
    public HttpResponse validateAndCreate(@JsonBody JSONObject request) {
        String accessToken = (String) request.get("accessToken");
        if(accessToken == null){
            throw new ServiceException.BadRequestException("accessToken is required");
        }

        accessToken = accessToken.trim();

        try {
            User authenticatedUser =  getAuthenticatedUser();

            HttpURLConnection connection = connect(String.format("%s/%s", getUri(), "user"),accessToken);
            validateAccessTokenScopes(connection);
            String data = IOUtils.toString(HttpRequest.getInputStream(connection), Charset.defaultCharset());
            GHUser user = GithubScm.getMappingObjectReader().forType(GHUser.class).readValue(data);

            if(user.getEmail() != null){
                Mailer.UserProperty p = authenticatedUser.getProperty(Mailer.UserProperty.class);
                //XXX: If there is already email address of this user, should we update it with
                // the one from Github?
                if (p==null){
                    authenticatedUser.addProperty(new Mailer.UserProperty(user.getEmail()));
                }
            }

            //Now we know the token is valid. Lets find credential
            String credentialId = createCredentialId(getUri());
            StandardUsernamePasswordCredentials githubCredential = CredentialsUtils.findCredential(credentialId, StandardUsernamePasswordCredentials.class, new BlueOceanDomainRequirement());
            final StandardUsernamePasswordCredentials credential = new UsernamePasswordCredentialsImpl(CredentialsScope.USER, credentialId, getCredentialDescription(), authenticatedUser.getId(), accessToken);

            if(githubCredential == null) {
                CredentialsUtils.createCredentialsInUserStore(
                    credential, authenticatedUser, getCredentialDomainName(),
                    Collections.singletonList(new BlueOceanDomainSpecification()));
            }else{
                CredentialsUtils.updateCredentialsInUserStore(
                        githubCredential, credential, authenticatedUser, getCredentialDomainName(),
                        Collections.singletonList(new BlueOceanDomainSpecification()));
            }

            return createResponse(credential.getId());

        } catch (IOException e) {
            if (e instanceof MalformedURLException || e instanceof UnknownHostException) {
                throw new ServiceException.BadRequestException(
                    new ErrorMessage(400, "Invalid apiUrl").add(
                        new ErrorMessage.Error("apiUrl", ErrorMessage.Error.ErrorCodes.INVALID.toString(), e.getMessage())
                    )
                );
            }
            throw new ServiceException.UnexpectedErrorException(e.getMessage());
        }
    }

    protected static HttpURLConnection connect(String apiUrl, String accessToken) throws IOException {
        HttpURLConnection connection = HttpRequest.get(apiUrl).withAuthorizationToken(accessToken).connect();
        int status = connection.getResponseCode();
        if(status == 401){
            throw new ServiceException.PreconditionRequired("Invalid accessToken");
        }
        if(status == 403){
            throw new ServiceException.PreconditionRequired("GitHub accessToken does not have required scopes. Expected scopes 'user:email, repo'");
        }
        if(status == 404){
            throw new ServiceException.NotFoundException(String.format("Remote server at %s responded with code 404.", apiUrl));
        }
        if(status != 200) {
            throw new ServiceException.BadRequestException(String.format("GitHub Api returned error: %s. Error message: %s.", connection.getResponseCode(), connection.getResponseMessage()));
        }

        return connection;
    }

    /**
     * Ensure any existing access token is valid and has the proper scopes.
     */
    protected void validateExistingAccessToken() {
        String credentialId = createCredentialId(getUri());
        StandardUsernamePasswordCredentials githubCredential = CredentialsUtils.findCredential(credentialId, StandardUsernamePasswordCredentials.class, new BlueOceanDomainRequirement());

        if (githubCredential != null) {
            HttpURLConnection connection;
            try {
                connection = connect(String.format("%s/%s", getUri(), "user"), githubCredential.getPassword().getPlainText());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            validateAccessTokenScopes(connection);
        }
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

    static void validateUserHasPushPermission(@Nonnull String apiUrl, @Nullable String accessToken, @Nullable String owner, @Nullable String repoName) {
        GHRepository repo;
        try {
            repo = HttpRequest.get(String.format("%s/repos/%s/%s", apiUrl, owner, repoName))
                .withAuthorizationToken(accessToken).to(GHRepository.class);
        } catch (IOException e) {
            throw new ServiceException.UnexpectedErrorException(String.format("Could not load repository metadata for %s/%s", owner, repoName), e);
        }
        if (!repo.hasPushAccess()) {
            throw new ServiceException.PreconditionRequired(String.format("You do not have permission to push changes to %s/%s", owner, repoName));
        }
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
