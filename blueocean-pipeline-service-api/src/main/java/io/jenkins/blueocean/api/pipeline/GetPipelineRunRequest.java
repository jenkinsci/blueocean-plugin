package io.jenkins.blueocean.api.pipeline;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.jenkins.blueocean.security.Identity;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Request for {@link PipelineService#getPipelineRun(Identity, GetPipelineRunRequest)}
 *
 * @author Vivek Pandey
 */
public final class GetPipelineRunRequest {
    @JsonProperty("organization")
    public final String organization;

    @JsonProperty("pipeline")
    public final String pipeline;

    /** run is run id, if null latest run is expected */
    @JsonProperty("run")
    public final String run;

    public GetPipelineRunRequest(@Nonnull @JsonProperty("organization") String organization,
                                 @Nonnull @JsonProperty("pipeline") String pipeline,
                                 @Nullable @JsonProperty("run") String run) {
        this.organization = organization;
        this.pipeline = pipeline;
        this.run = run;
    }
}
