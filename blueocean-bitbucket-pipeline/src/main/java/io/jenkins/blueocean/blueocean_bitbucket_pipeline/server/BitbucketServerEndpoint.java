package io.jenkins.blueocean.blueocean_bitbucket_pipeline.server;

import com.cloudbees.jenkins.plugins.bitbucket.endpoints.AbstractBitbucketEndpoint;
import io.jenkins.blueocean.rest.impl.pipeline.scm.ScmServerEndpoint;
import org.kohsuke.stapler.export.Exported;

/**
 * @author Vivek Pandey
 */
public class BitbucketServerEndpoint extends ScmServerEndpoint {
    public static final String MANAGE_HOOKS = "manageHooks";
    public static final String CREDENTIAL_ID = "credentialId";

    private final AbstractBitbucketEndpoint endpoint;

    public BitbucketServerEndpoint(AbstractBitbucketEndpoint endpoint) {
        this.endpoint = endpoint;
    }

    @Override
    public String getName() {
        return endpoint.getDisplayName();
    }

    @Override
    public String getApiUrl() {
        return endpoint.getServerUrl();
    }

    @Exported(name = MANAGE_HOOKS)
    public boolean isManageHooks(){
        return endpoint.isManageHooks();
    }

    @Exported(name = CREDENTIAL_ID)
    public String credentialId(){
        return endpoint.getCredentialsId();
    }
}
