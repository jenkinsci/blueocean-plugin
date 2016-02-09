package io.jenkins.blueocean.api.pipeline;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.jenkins.blueocean.security.Identity;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Request for {@link PipelineService#findPipelines(Identity, FindPipelinesRequest)}
 *
 * @author Vivek Pandey
 */
public final class FindPipelinesRequest {

    @JsonProperty("organization")
    public final String organization;

    @JsonProperty("pipeline")
    public final String pipeline;


    public FindPipelinesRequest(@Nonnull @JsonProperty("organization") String organization,
                                @Nullable @JsonProperty("pipeline") String pipeline) {
        this.organization = organization;
        this.pipeline = pipeline;
    }
}
