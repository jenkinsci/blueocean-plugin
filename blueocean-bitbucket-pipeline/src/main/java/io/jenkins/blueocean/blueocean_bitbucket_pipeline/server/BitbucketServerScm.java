package io.jenkins.blueocean.blueocean_bitbucket_pipeline.server;

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
public class BitbucketServerScm extends AbstractBitbucketScm {
    public static final String ID = "bitbucket-server";
    static final String DOMAIN_NAME="blueocean-bitbucket-server-domain";

    public BitbucketServerScm(Reachable parent) {
        super(parent);
    }

    @Nonnull
    @Override
    public String getId() {
        return ID;
    }

    @Nonnull
    @Override
    public String getUri() {
        return getApiUrlParameter();
    }

    @Override
    public ScmServerEndpointContainer getServers() {
        return new BitbucketServerEndpointContainer(this);
    }

    @Override
    protected  @Nonnull String createCredentialId(@Nonnull String apiUrl){
        return BitbucketCredentialUtils.computeCredentialId(null, ID, apiUrl);
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
            return new BitbucketServerScm(parent);
        }
    }
}
