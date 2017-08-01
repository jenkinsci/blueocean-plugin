package io.jenkins.blueocean.rest.model;

import io.jenkins.blueocean.rest.annotation.Capability;
import org.kohsuke.stapler.export.Exported;

import static io.jenkins.blueocean.rest.model.KnownCapabilities.BLUE_ORGANIZATION_FOLDER;

/**
 * Provides organization folder API
 *
 * @author Vivek Pandey
 */
@Capability(BLUE_ORGANIZATION_FOLDER)
public abstract class BlueOrganizationFolder extends BluePipelineFolder {

    private static final String SCAN_ALL_REPOS = "scanAllRepos";
    private static final String SCM_SOURCE = "scmSource";

    /**
     * Returns whether pipeline repo discovery was run on all repositories inside organization folder.
     *
     * Determination should be based on whether user gave explicit list of repos or pattern or repos
     * or provided include/exclude criteria to only scan specific set of repos.
     *
     */
    @Exported(name = SCAN_ALL_REPOS)
    public abstract boolean isScanAllRepos();

    /**
     * Get metadata about the SCM for this pipeline.
     */
    @Exported(name = SCM_SOURCE, inline = true)
    public abstract BlueScmSource getScmSource();

}
