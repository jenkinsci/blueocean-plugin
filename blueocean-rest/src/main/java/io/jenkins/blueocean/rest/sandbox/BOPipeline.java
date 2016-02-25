package io.jenkins.blueocean.rest.sandbox;

import org.kohsuke.stapler.export.Exported;

/**
 * Defines pipeline state and its routing
 *
 * @author Vivek Pandey
 */
public abstract class BOPipeline extends Resource {
    @Exported
    public abstract String getOrganization();

    /** Name of the pipeline */
    @Exported
    public abstract String getName();

    /**
     * Human readable name of this pipeline
     */
    @Exported
    public abstract String getDisplayName();

//    /** Set of branches available with this pipeline */
//    @JsonProperty("branches")
//    public final List<String> branches;
}
