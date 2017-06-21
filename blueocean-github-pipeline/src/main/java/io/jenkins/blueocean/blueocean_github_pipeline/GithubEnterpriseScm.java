package io.jenkins.blueocean.blueocean_github_pipeline;

import com.cloudbees.plugins.credentials.common.StandardUsernamePasswordCredentials;
import hudson.Extension;
import io.jenkins.blueocean.commons.ErrorMessage;
import io.jenkins.blueocean.commons.ServiceException;
import io.jenkins.blueocean.commons.stapler.TreeResponse;
import io.jenkins.blueocean.credential.CredentialsUtils;
import io.jenkins.blueocean.rest.Reachable;
import io.jenkins.blueocean.rest.impl.pipeline.credential.BlueOceanDomainRequirement;
import io.jenkins.blueocean.rest.impl.pipeline.scm.Scm;
import io.jenkins.blueocean.rest.impl.pipeline.scm.ScmFactory;
import org.apache.commons.codec.digest.DigestUtils;
import org.kohsuke.stapler.WebMethod;
import org.kohsuke.stapler.verb.GET;
import org.parboiled.common.StringUtils;

import javax.annotation.Nonnull;

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
    public @Nonnull String getId() {
        return ID;
    }

    @Override
    public @Nonnull String getUri() {
        String apiUri = getCustomApiUri();

        // NOTE: GithubEnterpriseScm requires that the apiUri be specified
        if (StringUtils.isEmpty(apiUri)) {
            throw new ServiceException.BadRequestException(new ErrorMessage(400, "apiUrl is required parameter"));
        }

        return apiUri;
    }

    @Override
    public String getCredentialId() {
        String credentialId = createCredentialId(getUri());
        StandardUsernamePasswordCredentials githubCredential = CredentialsUtils.findCredential(credentialId, StandardUsernamePasswordCredentials.class, new BlueOceanDomainRequirement());
        if(githubCredential != null){
            return githubCredential.getId();
        }
        return null;
    }

    @Override
    public String getCredentialDomainName() {
        return DOMAIN_NAME;
    }

    @WebMethod(name="") @GET @TreeResponse
    public Object getState() {
        // will produce a 400 if apiUrl wasn't sent
        getUri();
        return this;
    }


    @Override
    protected @Nonnull String createCredentialId(@Nonnull String apiUri) {
        return getId() + ":" + DigestUtils.sha256Hex(apiUri);
    }

    @Override
    protected @Nonnull String getCredentialDescription() {
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

        @Nonnull
        @Override
        public Scm getScm(Reachable parent) {
            return new GithubEnterpriseScm(parent);
        }
    }

}
