package io.jenkins.blueocean.blueocean_github_pipeline;

import io.jenkins.blueocean.rest.impl.pipeline.scm.ScmOrganization;
import org.kohsuke.github.GHOrganization;

/**
 * @author Vivek Pandey
 */
public class GithubOrganization extends ScmOrganization {

    private final GHOrganization ghOrganization;

    public GithubOrganization(GHOrganization ghOrganization) {
        this.ghOrganization = ghOrganization;
    }

    @Override
    public String getId() {
        return ghOrganization.getLogin();
    }

}
