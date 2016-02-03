package io.jenkins.blueocean.api.pipeline;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.jenkins.blueocean.api.pipeline.model.Run;

import javax.annotation.Nullable;

/**
 * Response for findPipelineRun() method
 *
 * @author Vivek Pandey
 */
final class GetPipelineRunResponse {
    @JsonProperty("run")
    public final Run run;

    public GetPipelineRunResponse(@Nullable @JsonProperty("run") Run run) {
        this.run = run;
    }
}
