package io.jenkins.blueocean.blueocean_github_pipeline;

import hudson.Extension;
import io.jenkins.blueocean.rest.Reachable;
import io.jenkins.blueocean.rest.impl.pipeline.scm.Scm;
import io.jenkins.blueocean.rest.impl.pipeline.scm.ScmFactory;

import javax.annotation.Nonnull;

/**
 * @author Vivek Pandey
 */
public class GithubEnterpriseScm extends GithubScm {
    static final String DEFAULT_ENTERPRISE_API_SUFFIX = "/api/v3";
    static final String ID = "github-enterprise";
    static final String DOMAIN_NAME="blueocean-github-enterprise-domain";


    public GithubEnterpriseScm(Reachable parent) {
        super(parent);
    }

    @Override
    public @Nonnull String getId() {
        return ID;
    }

    @Override
    public @Nonnull String getUri() {
        return super.getUri()+DEFAULT_ENTERPRISE_API_SUFFIX;
    }

    @Override
    public String getCredentialDomainName() {
        return DOMAIN_NAME;
    }

    @Extension
    public static class GithubScmFactory extends ScmFactory {

        @Override
        public Scm getScm(String id, Reachable parent) {
            if(id.equals(ID)){
                return new GithubScm(parent);
            }
            return null;
        }

        @Nonnull
        @Override
        public Scm getScm(Reachable parent) {
            return new GithubScm(parent);
        }
    }

}
