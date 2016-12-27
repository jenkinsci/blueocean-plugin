package io.jenkins.blueocean.rest.impl.pipeline.scm;

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

    /** organization id */
    @Exported
    public abstract String getId();

}
