package io.jenkins.blueocean.blueocean_bitbucket_pipeline.server;

import com.cloudbees.jenkins.plugins.bitbucket.endpoints.AbstractBitbucketEndpoint;
import io.jenkins.blueocean.rest.impl.pipeline.scm.ScmServerEndpoint;

/**
 * @author Vivek Pandey
 */
public class BitbucketServerEndpoint extends ScmServerEndpoint {
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
}
