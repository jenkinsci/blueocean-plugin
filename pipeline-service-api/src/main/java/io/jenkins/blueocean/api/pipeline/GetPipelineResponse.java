package io.jenkins.blueocean.api.pipeline;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.jenkins.blueocean.api.pipeline.model.Pipeline;
import io.jenkins.blueocean.commons.Identity;

import javax.annotation.Nullable;

/**
 * Response for {@link PipelineService#getPipeline(Identity, GetPipelineRequest)}
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
