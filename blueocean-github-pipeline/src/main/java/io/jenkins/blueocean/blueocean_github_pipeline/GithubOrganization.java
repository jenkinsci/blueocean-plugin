package io.jenkins.blueocean.blueocean_github_pipeline;

import io.jenkins.blueocean.rest.hal.Link;
import io.jenkins.blueocean.rest.impl.pipeline.scm.ScmOrganization;
import org.kohsuke.github.GHOrganization;

/**
 * @author Vivek Pandey
 */
public class GithubOrganization extends ScmOrganization {

    private final GHOrganization ghOrganization;
    private final Link self;

    public GithubOrganization(GHOrganization ghOrganization, Link parent) {
        this.ghOrganization = ghOrganization;
        this.self = parent.rel(ghOrganization.getLogin());
    }

    @Override
    public String getId() {
        return ghOrganization.getLogin();
    }

    @Override
    public Link getLink() {
        return self;
    }
}
