package io.jenkins.blueocean.api.pipeline;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.jenkins.blueocean.api.pipeline.model.Run;
import io.jenkins.blueocean.commons.Identity;

import javax.annotation.Nullable;

/**
 * Response for {@link PipelineService#getPipelineRun(Identity, GetPipelineRunRequest)}
 *
 * @author Vivek Pandey
 */
public final class GetPipelineRunResponse {
    @JsonProperty("run")
    public final Run run;

    public GetPipelineRunResponse(@Nullable @JsonProperty("run") Run run) {
        this.run = run;
    }
}
