package io.jenkins.blueocean.rest.impl.pipeline.scm;

import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.export.ExportedBean;

/**
 *
 * SCM organization.
 *
 * Certain SCM provider support organization concept, such as github, bitbucket but not all.
 *
 * @author Vivek Pandey
 */
@ExportedBean
public abstract class ScmOrganization {

    /** organization id */
    @Exported
    public abstract String getId();

}
