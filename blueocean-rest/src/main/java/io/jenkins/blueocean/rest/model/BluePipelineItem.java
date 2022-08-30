package io.jenkins.blueocean.rest.model;

import io.jenkins.blueocean.rest.Navigable;
import io.jenkins.blueocean.rest.Reachable;
import io.jenkins.blueocean.rest.annotation.Capability;
import org.kohsuke.stapler.export.Exported;

import edu.umd.cs.findbugs.annotations.NonNull;
import java.util.Collection;

import static io.jenkins.blueocean.rest.model.KnownCapabilities.BLUE_PIPELINE;

/**
 * Minimal interface for all Blue Ocean items that live in the "pipeline namespace"
 * <p>
 * This includes various kinds of jobs that can be run, but also things that exist only as containers like folders and
 * multibranch parents, and as such doesn't contain any references to runs, build times, etc.
 */
@Capability(BLUE_PIPELINE)
public interface BluePipelineItem extends Reachable {
    /**
     * @return the organization that owns this item
     */
    @NonNull
    BlueOrganization getOrganization();

    /**
     * @return name of the organization that owns this item
     */
    @Exported(name = "organization")
    String getOrganizationName();

    /**
     * @return name of the pipeline
     */
    @Exported(name = "name")
    String getName();

    /**
     * @return human readable name of this pipeline
     */
    @Exported(name = "displayName")
    String getDisplayName();

    /**
     * @return Includes parent folders names if any. For example folder1/folder2/p1
     */
    @Exported(name = "fullName")
    String getFullName();

    /**
     * @return Includes display names of parent folders if any. For example folder1/myFolder2/p1
     */
    @Exported(name = "fullDisplayName")
    String getFullDisplayName();

    /**
     * @return Gives Actions associated with this Run
     */
    @Navigable
    @Exported(name = "actions", inline = true)
    Collection<BlueActionProxy> getActions();

}
