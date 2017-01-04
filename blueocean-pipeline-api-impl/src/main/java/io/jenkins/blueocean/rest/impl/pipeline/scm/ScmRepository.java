package io.jenkins.blueocean.rest.impl.pipeline.scm;

import io.jenkins.blueocean.rest.model.Resource;
import org.kohsuke.stapler.export.Exported;

import java.util.Map;

/**
 * SCM repository
 *
 * @author Vivek Pandey
 */
public abstract class ScmRepository extends Resource {
    public static final String NAME="name";
    public static final String PRIVATE="private";
    public static final String DESCRIPTION="description";
    public static final String DEFAULT_BRANCH = "defaultBranch";
    public static final String PERMISSIONS="permissions";

    /**
     * Name of repository
     */
    @Exported(name = NAME)
    public abstract String getName();

    /** Whether this repository is private */
    @Exported(name = PRIVATE)
    public abstract boolean isPrivate();

    /** Description of SCM  repo */
    @Exported(name = DESCRIPTION)
    public abstract String getDescription();

    /** Default branch of this repo */
    @Exported(name = DEFAULT_BRANCH)
    public abstract String getDefaultBranch();

    /** Permissions attached to this SCM repo */
    @Exported(name = PERMISSIONS)
    public abstract Map<String, Boolean> getPermissions();
}
