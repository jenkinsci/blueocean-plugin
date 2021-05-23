package io.jenkins.blueocean.blueocean_git_pipeline;

import com.cloudbees.plugins.credentials.CredentialsMatchers;
import com.cloudbees.plugins.credentials.CredentialsProvider;
import com.cloudbees.plugins.credentials.CredentialsScope;
import com.cloudbees.plugins.credentials.common.StandardCredentials;
import com.cloudbees.plugins.credentials.common.StandardUsernamePasswordCredentials;
import com.cloudbees.plugins.credentials.domains.DomainRequirement;
import com.cloudbees.plugins.credentials.domains.DomainSpecification;
import com.cloudbees.plugins.credentials.impl.UsernamePasswordCredentialsImpl;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.Extension;
import hudson.model.User;
import hudson.util.HttpResponses;
import io.jenkins.blueocean.commons.DigestUtils;
import io.jenkins.blueocean.commons.ErrorMessage;
import io.jenkins.blueocean.commons.ServiceException;
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
import jenkins.plugins.git.AbstractGitSCMSource;
import jenkins.plugins.git.GitSCMFileSystem;
import jenkins.plugins.git.GitSCMSource;
import jenkins.scm.api.SCMSourceOwner;
import net.sf.json.JSONException;
import net.sf.json.JSONObject;
import org.eclipse.jgit.lib.Repository;
import org.kohsuke.stapler.HttpResponse;
import org.kohsuke.stapler.Stapler;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.json.JsonBody;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Locale;

public class GitScm extends AbstractScm {

    public static final String ID = "git";

    static final String CREDENTIAL_DOMAIN_NAME = "blueocean-git-domain";
    static final String CREDENTIAL_DESCRIPTION_PW = "Git username/password";

    protected final Reachable parent;

    public GitScm(Reachable parent) {
        this.parent = parent;
    }

    /**
     * Create the credentialId for a specific repositoryUrl (which will be normalized)
     * @param repositoryUrl
     * @return credentialId string
     */
    public static String makeCredentialId(String repositoryUrl) {

        final String normalizedUrl = normalizeServerUrl(repositoryUrl);

        try {
            final java.net.URI uri = new URI(normalizedUrl);

            // Require a host
            String host = uri.getHost();
            if (host == null || host.length() == 0) {
                return null;
            }

            // Only http(s) urls have a default credential ID keyed to the repo right now
            String scheme = uri.getScheme();
            if (scheme != null && scheme.startsWith("http")) {
                return String.format( "%s:%s", ID, DigestUtils.sha256Hex(normalizedUrl));
            }
        } catch (URISyntaxException e) {
            // Fall through
        }

        // Bad URL, or not a http(s) url
        return null;
    }

    /**
     * Normalize the URL to protocol, port, and host to use as part of the credential id
     * @param repositoryUrl
     * @return a normalized url without path, query, or fragment components
     */
    private static String normalizeServerUrl(String repositoryUrl) {

        if (repositoryUrl == null) {
            return "";
        }

        try {
            java.net.URI uri = new URI(repositoryUrl).normalize();
            String scheme = uri.getScheme();

            String host = uri.getHost() == null ? null : uri.getHost().toLowerCase(Locale.ENGLISH);
            int port = uri.getPort();
            if ("http".equals(scheme) && port == 80) {
                port = -1;
            } else if ("https".equals(scheme) && port == 443) {
                port = -1;
            } else if ("ssh".equals(scheme) && port == 22) {
                port = -1;
            } else if ("git".equals(scheme) && port == 9418) {
                port = -1;
            }
            return new URI(
                scheme,
                uri.getUserInfo(),
                host,
                port,
                null,
                null,
                null
            ).toASCIIString().replaceAll("/$", "");
        } catch (URISyntaxException e) {
            // Bad URL
            return "";
        }
    }

    @Override
    public Link getLink() {
        return parent.getLink().rel("git");
    }

    @Override
    @NonNull
    public String getId() {
        return ID;
    }

    @Override
    @NonNull
    public String getUri() {
        return "";
    }

    protected StaplerRequest getStaplerRequest() {
        StaplerRequest request = Stapler.getCurrentRequest();
        Preconditions.checkNotNull(request, "Must be called in HTTP request context");
        return request;
    }

    @Override
    public String getCredentialId() {
        // Only return the generated id if we actually have a credential that matches it
        StandardCredentials credential = getCredentialForCurrentRequest();
        if (credential != null) {
            return credential.getId();
        }
        return null;
    }

    protected StandardCredentials getCredentialForCurrentRequest() {
        final StaplerRequest request = getStaplerRequest();

        String credentialId = null;

        if (request.hasParameter("credentialId")) {
            credentialId = request.getParameter("credentialId");
        } else {
            if (!request.hasParameter("repositoryUrl")) {
                // No linked credential unless a specific repo
                return null;
            }

            String repositoryUrl = request.getParameter("repositoryUrl");
            credentialId = makeCredentialId(repositoryUrl);
        }

        if (credentialId == null) {
            return null;
        }

        return CredentialsUtils.findCredential(credentialId, StandardCredentials.class, new BlueOceanDomainRequirement());
    }

    @Override
    public Container<ScmOrganization> getOrganizations() {
        return null;
    }

    @Override
    public ScmServerEndpointContainer getServers() {
        return null;
    }

    @Override
    public HttpResponse validateAndCreate(@JsonBody JSONObject request) {

        boolean requirePush = request.has("requirePush");

        // --[ Grab repo url and SCMSource ]----------------------------------------------------------

        final String repositoryUrl;
        final AbstractGitSCMSource scmSource;

        if (request.has("repositoryUrl")) {
            repositoryUrl = request.getString("repositoryUrl");
            scmSource = new GitSCMSource(repositoryUrl);
        } else {
            try {
                String fullName = request.getJSONObject("pipeline").getString("fullName");
                SCMSourceOwner item = Jenkins.getInstance().getItemByFullName(fullName, SCMSourceOwner.class);
                if (item != null) {
                    scmSource = (AbstractGitSCMSource) item.getSCMSources().iterator().next();
                    repositoryUrl = scmSource.getRemote();
                } else {
                    return HttpResponses.errorJSON("No repository found for: " + fullName);
                }
            } catch (JSONException e) {
                return HttpResponses.errorJSON("No repositoryUrl or pipeline.fullName specified in request.");
            } catch (RuntimeException e) {
                return HttpResponses.errorWithoutStack(ServiceException.INTERNAL_SERVER_ERROR, e.getMessage());
            }
        }

        // --[ Grab user ]-------------------------------------------------------------------------------------

        User user = User.current();
        if (user == null) {
            throw new ServiceException.UnauthorizedException("Not authenticated");
        }

        // --[ Get credential id from request or create from repo url ]----------------------------------------

        String credentialId = null;

        if (request.has("credentialId")) {
            credentialId = request.getString("credentialId");
        }

        if (credentialId == null) {
            credentialId = makeCredentialId(repositoryUrl);
        }

        if (credentialId == null) {
            // Still null? Must be a bad repoURL
            throw new ServiceException.BadRequestException("Invalid URL \"" + repositoryUrl + "\"");
        }

        // --[ Load or create credentials ]--------------------------------------------------------------------

        // Create new is only for username + password
        if (request.has("userName") || request.has("password")) {
            createPWCredentials(credentialId, user, request, repositoryUrl);
        }

        final StandardCredentials creds = CredentialsMatchers.firstOrNull(
            CredentialsProvider.lookupCredentials(
                StandardCredentials.class,
                Jenkins.getInstance(),
                Jenkins.getAuthentication(),
                (List<DomainRequirement>) null),
            CredentialsMatchers.allOf(CredentialsMatchers.withId(credentialId))
        );

        if (creds == null) {
            throw new ServiceException.NotFoundException("No credentials found for: " + credentialId);
        }

        try {
            if (requirePush) {
                String branch = request.getString("branch");
                if (repositoryUrl != null){
                    ((GitSCMSource) scmSource).setCredentialsId(credentialId);
                }
                new GitBareRepoReadSaveRequest(scmSource, branch, null, branch, null, null)
                    .invokeOnScm(new GitSCMFileSystem.FSFunction<Void>() {
                        @Override
                        public Void invoke(Repository repository) throws IOException, InterruptedException {
                            GitUtils.validatePushAccess(repository, repositoryUrl, creds);
                            return null;
                        }
                    });
            } else {
                List<ErrorMessage.Error> errors = GitUtils.validateCredentials(repositoryUrl, creds);
                if (!errors.isEmpty()) {
                    throw new ServiceException.UnauthorizedException(errors.get(0).getMessage());
                }
            }
        } catch (Exception e) {
            String message = e.getMessage();
            if (message != null && message.contains("TransportException")) {
                throw new ServiceException.PreconditionRequired("Repository URL unreachable: " + repositoryUrl);
            }

            return HttpResponses.errorWithoutStack(ServiceException.PRECONDITION_REQUIRED, message);
        }

        return HttpResponses.okJSON();
    }

    private void createPWCredentials(String credentialId, User user, @JsonBody JSONObject request, String repositoryUrl) {

        StandardUsernamePasswordCredentials existingCredential =
            CredentialsUtils.findCredential(credentialId,
                                            StandardUsernamePasswordCredentials.class,
                                            new BlueOceanDomainRequirement());

        String requestUsername = request.getString("userName");
        String requestPassword = request.getString("password");

        // Un-normalized repositoryUrl so the description matches user input.
        String description = String.format("%s for %s", CREDENTIAL_DESCRIPTION_PW, repositoryUrl);

        final StandardUsernamePasswordCredentials newCredential =
            new UsernamePasswordCredentialsImpl(CredentialsScope.USER,
                                                credentialId,
                                                description,
                                                requestUsername,
                                                requestPassword);

        try {
            if (existingCredential == null) {
                CredentialsUtils.createCredentialsInUserStore(newCredential,
                                                              user,
                                                              CREDENTIAL_DOMAIN_NAME,
                                                              ImmutableList.<DomainSpecification>of(new BlueOceanDomainSpecification()));
            } else {
                CredentialsUtils.updateCredentialsInUserStore(existingCredential,
                                                              newCredential,
                                                              user,
                                                              CREDENTIAL_DOMAIN_NAME,
                                                              ImmutableList.<DomainSpecification>of(new BlueOceanDomainSpecification()));
            }
        } catch (IOException e) {
            throw new ServiceException.UnexpectedErrorException("Could not persist credential", e);
        }
    }

    @Extension
    public static class GitScmFactory extends ScmFactory {
        @Override
        public Scm getScm(@NonNull String id, @NonNull Reachable parent) {
            if (id.equals(ID)) {
                return new GitScm(parent);
            }
            return null;
        }

        @NonNull
        @Override
        public Scm getScm(Reachable parent) {
            return new GitScm(parent);
        }
    }


}
