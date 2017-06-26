package io.jenkins.blueocean.blueocean_bitbucket_pipeline.cloud;

import io.jenkins.blueocean.blueocean_bitbucket_pipeline.AbstractBitbucketScm;
import io.jenkins.blueocean.rest.Reachable;

import javax.annotation.Nonnull;

/**
 * @author Vivek Pandey
 */
public class BitbucketCloudScm extends AbstractBitbucketScm {
    static final String DOMAIN_NAME="blueocean-bitbucket-cloud-domain";
    static final String ID = "bitbucket-cloud";
    static final String API_URL = "https://api.bitbucket.org/";

    public BitbucketCloudScm(Reachable parent) {
        super(parent);
    }

    @Nonnull
    @Override
    public String getId() {
        return ID;
    }

    @Nonnull
    @Override
    protected String createCredentialId(@Nonnull String apiUrl) {
        return ID;
    }

    @Nonnull
    @Override
    protected String getDomainId() {
        return DOMAIN_NAME;
    }
}
