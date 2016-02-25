package io.jenkins.blueocean.rest.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.kohsuke.stapler.export.Exported;

/**
 * Defines pipeline state and its routing
 *
 * @author Vivek Pandey
 */
public abstract class BOPipeline extends Resource {
    public static final String ORGANIZATION="organization";
    public static final String NAME="name";
    public static final String DISPLAY_NAME="displayName";
    public static final String BRANCHES="branches";
    public static final String RUNS="runs";

    /**
     * @return name of the organization
     */
    @Exported(name = ORGANIZATION)
    @JsonProperty(ORGANIZATION)
    public abstract String getOrganization();

    /**
     * @return name of the pipeline
     */
    @Exported(name = NAME)
    @JsonProperty(NAME)
    public abstract String getName();

    /**
     * @return human readable name of this pipeline
     */
    @Exported(name = DISPLAY_NAME)
    @JsonProperty(DISPLAY_NAME)
    public abstract String getDisplayName();

    //TODO: collections should serailize as reference to the resource or pagination can't be done

    /**
     * @return Set of branches available with this pipeline
     */
    @JsonProperty(BRANCHES)
    public abstract BOBranchContainer getBranches();

    /**
     * @return Gives Runs in this pipeline
     */
    @JsonProperty(RUNS)
    public abstract BORunContainer getRuns();

}
