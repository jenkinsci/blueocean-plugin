package io.jenkins.blueocean.blueocean_github_pipeline;

import com.cloudbees.plugins.credentials.common.StandardUsernamePasswordCredentials;
import com.google.common.base.Preconditions;
import hudson.Extension;
import hudson.model.User;
import io.jenkins.blueocean.commons.ErrorMessage;
import io.jenkins.blueocean.commons.ServiceException;
import io.jenkins.blueocean.credential.CredentialsUtils;
import io.jenkins.blueocean.rest.Reachable;
import io.jenkins.blueocean.rest.impl.pipeline.scm.Scm;
import io.jenkins.blueocean.rest.impl.pipeline.scm.ScmFactory;
import org.kohsuke.stapler.Stapler;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.export.Exported;
import org.parboiled.common.StringUtils;

import javax.annotation.Nonnull;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Vivek Pandey
 */
public class GithubEnterpriseScm extends GithubScm {
    static final String ID = "github-enterprise";
    static final String DOMAIN_NAME="blueocean-github-enterprise-domain";
    private static final String CREDENTIALS = "credentials";


    public GithubEnterpriseScm(Reachable parent) {
        super(parent);
    }

    @Override
    public @Nonnull String getId() {
        return ID;
    }

    @Override
    public @Nonnull String getUri() {
        String apiUri = getCustomApiUri();

        // NOTE: GithubEnterpriseScm requires that the apiUri be specified
        if (StringUtils.isEmpty(apiUri)) {
            throw new ServiceException.BadRequestException(new ErrorMessage(400, "URI is required"));
        }

        return apiUri;
    }

    @Override
    public String getCredentialId() {
        // there is no "default" credential for GitHub Enterprise, so return null
        return null;
    }

    @Override
    public String getCredentialDomainName() {
        return DOMAIN_NAME;
    }

    @Exported(name = CREDENTIALS)
    public List<GithubEnterpriseScmCredential> getCredentials() {
        List<GithubEnterpriseScmCredential> credentialInfo = new ArrayList<>();

        User user = getAuthenticatedUser();
        List<StandardUsernamePasswordCredentials> credentials = CredentialsUtils.findCredentials(StandardUsernamePasswordCredentials.class, user, DOMAIN_NAME);

        for (StandardUsernamePasswordCredentials cred : credentials) {
            credentialInfo.add(new GithubEnterpriseScmCredential(cred));
        }

        return credentialInfo;
    }

    @Override
    protected String createCredentialId(@Nonnull String apiUri) {
        String domainName = getCredentialDomainName();
        return domainName + ":" + apiUri;
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

        @Nonnull
        @Override
        public Scm getScm(Reachable parent) {
            return new GithubEnterpriseScm(parent);
        }
    }

}
