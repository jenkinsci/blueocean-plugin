package io.jenkins.blueocean.rest.model;

import io.jenkins.blueocean.rest.Navigable;
import io.jenkins.blueocean.rest.Reachable;
import org.kohsuke.stapler.export.Exported;

import javax.annotation.Nonnull;
import java.util.Collection;

/**
 * Minimal interface for all Blue Ocean items that live in the "pipeline namespace"
 * <p>
 * This includes various kinds of jobs that can be run, but also things that exist only as containers like folders and
 * multibranch parents, and as such doesn't contain any references to runs, build times, etc.
 */
public interface BluePipelineItem extends Reachable {
    /**
     * @return the organization that owns this item
     */
    @Nonnull
    BlueOrganization getOrganization();

    /**
     * @return name of the organization that owns this item
     */
    @Exported(name = BluePipeline.ORGANIZATION)
    String getOrganizationName();

    /**
     * @return name of the pipeline
     */
    @Exported(name = BluePipeline.NAME)
    String getName();

    /**
     * @return human readable name of this pipeline
     */
    @Exported(name = BluePipeline.DISPLAY_NAME)
    String getDisplayName();

    /**
     * @return Includes parent folders names if any. For example folder1/folder2/p1
     */
    @Exported(name = BluePipeline.FULL_NAME)
    String getFullName();

    /**
     * @return Includes display names of parent folders if any. For example folder1/myFolder2/p1
     */
    @Exported(name = BluePipeline.FULL_DISPLAY_NAME)
    String getFullDisplayName();

    /**
     * @return Gives Actions associated with this Run
     */
    @Navigable
    @Exported(name = BluePipeline.ACTIONS, inline = true)
    Collection<BlueActionProxy> getActions();

}
