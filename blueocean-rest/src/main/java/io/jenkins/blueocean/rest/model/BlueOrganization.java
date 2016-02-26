package io.jenkins.blueocean.rest.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.kohsuke.stapler.export.Exported;

/**
 * API endpoint for an organization that houses all the pipelines.
 *
 * @author Kohsuke Kawaguchi
 */
public abstract class BlueOrganization extends Resource {
    public static final String NAME="name";
    public static final String PIPELINES="pipelines";

    @Exported(name = NAME)
    @JsonProperty(NAME)
    public abstract String getName();

    @JsonProperty(PIPELINES)
    public abstract BluePipelineContainer getPipelines();

}

