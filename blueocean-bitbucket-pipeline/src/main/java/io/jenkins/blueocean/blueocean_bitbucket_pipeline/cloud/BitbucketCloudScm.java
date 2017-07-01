package io.jenkins.blueocean.blueocean_bitbucket_pipeline.cloud;

import hudson.Extension;
import io.jenkins.blueocean.blueocean_bitbucket_pipeline.AbstractBitbucketScm;
import io.jenkins.blueocean.rest.Reachable;
import io.jenkins.blueocean.rest.impl.pipeline.scm.Scm;
import io.jenkins.blueocean.rest.impl.pipeline.scm.ScmServerEndpointContainer;
import io.jenkins.blueocean.rest.impl.pipeline.scm.ScmFactory;

import javax.annotation.Nonnull;

/**
 * @author Vivek Pandey
 */
public class BitbucketCloudScm extends AbstractBitbucketScm {
    static final String DOMAIN_NAME="blueocean-bitbucket-cloud-domain";
    static final String ID = "bitbucket-cloud";
    static final String API_URL = "https://bitbucket.org/api/2.0/";

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
        return ID;
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
