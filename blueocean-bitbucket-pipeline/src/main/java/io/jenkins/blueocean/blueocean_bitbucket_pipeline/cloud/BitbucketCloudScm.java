package io.jenkins.blueocean.blueocean_bitbucket_pipeline.cloud;

import hudson.Extension;
import io.jenkins.blueocean.blueocean_bitbucket_pipeline.AbstractBitbucketScm;
import io.jenkins.blueocean.blueocean_bitbucket_pipeline.BitbucketCredentialUtils;
import io.jenkins.blueocean.rest.Reachable;
import io.jenkins.blueocean.rest.impl.pipeline.scm.Scm;
import io.jenkins.blueocean.rest.impl.pipeline.scm.ScmFactory;
import io.jenkins.blueocean.rest.impl.pipeline.scm.ScmServerEndpointContainer;

import javax.annotation.Nonnull;

/**
 * @author Vivek Pandey
 */
public class BitbucketCloudScm extends AbstractBitbucketScm {
    public static final String ID = "bitbucket-cloud";
    public static final String API_URL = "https://bitbucket.org";
    static final String DOMAIN_NAME="blueocean-bitbucket-cloud-domain";

    public BitbucketCloudScm(Reachable parent) {
        super(parent);
    }

    @Nonnull
    @Override
    public String getId() {
        return ID;
    }

    @Override
    public ScmServerEndpointContainer getServers() {
        return null;
    }

    @Nonnull
    @Override
    protected String createCredentialId(@Nonnull String apiUrl) {
        return BitbucketCredentialUtils.computeCredentialId(ID, apiUrl);
    }

    @Nonnull
    @Override
    protected String getDomainId() {
        return DOMAIN_NAME;
    }

    @Extension
    public static class BbScmFactory extends ScmFactory {
        @Override
        public Scm getScm(String id, Reachable parent) {
            if(id.equals(ID)){
                return getScm(parent);
            }
            return null;
        }

        @Nonnull
        @Override
        public Scm getScm(Reachable parent) {
            return new BitbucketCloudScm(parent);
        }
    }
}
