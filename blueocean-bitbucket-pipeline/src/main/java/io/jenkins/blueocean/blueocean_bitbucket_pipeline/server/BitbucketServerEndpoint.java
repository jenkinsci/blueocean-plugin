package io.jenkins.blueocean.blueocean_bitbucket_pipeline.server;

import com.cloudbees.jenkins.plugins.bitbucket.endpoints.AbstractBitbucketEndpoint;
import io.jenkins.blueocean.rest.impl.pipeline.scm.ScmServerEndpoint;
import org.apache.commons.lang.StringUtils;

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
        return StringUtils.isBlank(endpoint.getDisplayName()) ?  endpoint.getServerUrl() : endpoint.getDisplayName();
    }

    @Override
    public String getApiUrl() {
        return endpoint.getServerUrl();
    }
}
