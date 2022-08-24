package io.jenkins.blueocean.blueocean_github_pipeline;

import com.cloudbees.plugins.credentials.common.StandardUsernamePasswordCredentials;
import hudson.Extension;
import io.jenkins.blueocean.commons.ErrorMessage;
import io.jenkins.blueocean.commons.ServiceException;
import io.jenkins.blueocean.rest.Navigable;
import io.jenkins.blueocean.rest.Reachable;
import io.jenkins.blueocean.rest.hal.Link;
import io.jenkins.blueocean.rest.impl.pipeline.scm.Scm;
import io.jenkins.blueocean.rest.impl.pipeline.scm.ScmFactory;
import io.jenkins.blueocean.rest.impl.pipeline.scm.ScmServerEndpointContainer;

import edu.umd.cs.findbugs.annotations.NonNull;

/**
 * @author Vivek Pandey
 */
public class GithubEnterpriseScm extends GithubScm {
    static final String ID = "github-enterprise";
    static final String DOMAIN_NAME="blueocean-github-enterprise-domain";
    static final String CREDENTIAL_DESCRIPTION = "GitHub Enterprise Access Token";

    public GithubEnterpriseScm(Reachable parent) {
        super(parent);
    }

    @Override
    public @NonNull String getId() {
        return ID;
    }

    @Override
    public @NonNull String getUri() {
        String apiUri = getCustomApiUri();

        // NOTE: GithubEnterpriseScm requires that the apiUri be specified
        if (apiUri == null || apiUri.isEmpty()) {
            throw new ServiceException.BadRequestException(new ErrorMessage(400, "apiUrl is required parameter"));
        }

        return apiUri;
    }

    @Override
    public String getCredentialId() {
        StandardUsernamePasswordCredentials githubCredential = getCredential(getUri());
        if(githubCredential != null){
            return githubCredential.getId();
        }
        return null;
    }

    @Override
    public String getCredentialDomainName() {
        return DOMAIN_NAME;
    }

    @Override
    public boolean isOrganizationAvatarSupported() {
        return false;
    }

    @Override
    public Object getState() {
        // will produce a 400 if apiUrl wasn't sent
        getUri();
        return super.getState();
    }

    @Navigable
    public ScmServerEndpointContainer getServers() {
        return new GithubServerContainer(getLink());
    }

    @Override
    protected @NonNull String createCredentialId(@NonNull String apiUri) {
        return GithubCredentialUtils.computeCredentialId(null, GithubEnterpriseScm.ID, apiUri);
    }

    @Override
    protected @NonNull String getCredentialDescription() {
        return CREDENTIAL_DESCRIPTION;
    }

    @Extension
    public static class GithubScmFactory extends ScmFactory {

        @Override
        public Scm getScm(String id, Reachable parent) {
            if(id.equals(ID)){
                return new GithubEnterpriseScm(parent);
            }
            return null;
        }

        @NonNull
        @Override
        public Scm getScm(Reachable parent) {
            return new GithubEnterpriseScm(parent);
        }
    }

    @Override
    public Link getLink() {
        return parent.getLink().rel("github-enterprise");
    }
}
