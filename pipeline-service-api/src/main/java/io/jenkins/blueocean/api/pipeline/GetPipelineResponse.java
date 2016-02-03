package io.jenkins.blueocean.api.pipeline;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.jenkins.blueocean.api.pipeline.model.Pipeline;

import javax.annotation.Nullable;

/**
 * findPipeline() response
 *
 * @author Vivek Pandey
 */
public final class GetPipelineResponse {

    @JsonProperty("pipeline")
    public final Pipeline pipeline;

    public GetPipelineResponse(@Nullable @JsonProperty("pipeline") Pipeline pipeline) {
        this.pipeline = pipeline;
    }
}
