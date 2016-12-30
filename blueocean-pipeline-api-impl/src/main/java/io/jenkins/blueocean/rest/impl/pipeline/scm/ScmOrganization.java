package io.jenkins.blueocean.rest.impl.pipeline.scm;

import io.jenkins.blueocean.rest.Navigable;
import io.jenkins.blueocean.rest.model.Resource;
import org.kohsuke.stapler.export.Exported;

/**
 *
 * SCM organization.
 *
 * Certain SCM provider support organization concept, such as github, bitbucket but not all.
 *
 * @author Vivek Pandey
 */
public abstract class ScmOrganization extends Resource{
    public static final String NAME ="name";
    private static final String IS_JENKINS_ORG_PIPELINE = "jenkinsOrganizationPipeline";

    /** organization id */
    @Exported(name = NAME)
    public abstract String getName();

    @Exported(name = IS_JENKINS_ORG_PIPELINE)
    public abstract boolean isJenkinsOrganizationPipeline();

    /** SCM repositories */
    @Navigable
    public abstract ScmRepositoryContainer getRepositories();
}
