package io.jenkins.blueocean.api.pipeline;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.jenkins.blueocean.commons.Identity;

import javax.annotation.Nonnull;

/**
 * Request for {@link PipelineService#getPipeline(Identity, GetPipelineRequest)}
 *
 * @author Vivek Pandey
 */
public final class GetPipelineRequest {

    @JsonProperty("organization")
    public final String organization;

    @JsonProperty("pipeline")
    public final String pipeline;


    public GetPipelineRequest(@Nonnull @JsonProperty("organization") String organization,
                              @Nonnull @JsonProperty("pipeline") String pipeline) {
        this.organization = organization;
        this.pipeline = pipeline;
    }
}
