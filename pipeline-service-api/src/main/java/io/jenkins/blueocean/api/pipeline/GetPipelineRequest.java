package io.jenkins.blueocean.api.pipeline;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.annotation.Nonnull;

/**
 * findPipeline() request
 *
 * @author Vivek Pandey
 */
final class GetPipelineRequest {

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
