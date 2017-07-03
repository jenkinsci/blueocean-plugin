package io.jenkins.blueocean.blueocean_github_pipeline;

import io.jenkins.blueocean.rest.hal.Link;
import io.jenkins.blueocean.rest.impl.pipeline.scm.ScmOrganization;
import io.jenkins.blueocean.rest.impl.pipeline.scm.ScmRepositoryContainer;

/**
 * @author cliffmeyers
 */
public class GithubEnterpriseOrganization extends ScmOrganization {

    private final ScmOrganization wrapped;

    public GithubEnterpriseOrganization(ScmOrganization wrapped) {
        this.wrapped = wrapped;
    }

    @Override
    public String getName() {
        return wrapped.getName();
    }

    @Override
    public String getAvatar() {
        // GitHub Enterprise requires user to be authenticated to view org images
        // as Blue Ocean user likely won't be auth'd, return null as a clue to the UI to show placeholder
        return null;
    }

    @Override
    public boolean isJenkinsOrganizationPipeline() {
        return wrapped.isJenkinsOrganizationPipeline();
    }

    @Override
    public ScmRepositoryContainer getRepositories() {
        return wrapped.getRepositories();
    }

    @Override
    public Link getLink() {
        return wrapped.getLink();
    }
}
