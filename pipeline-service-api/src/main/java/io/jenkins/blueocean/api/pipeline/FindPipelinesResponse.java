package io.jenkins.blueocean.api.pipeline;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableList;
import io.jenkins.blueocean.api.pipeline.model.Pipeline;
import io.jenkins.blueocean.commons.Identity;

import java.util.List;

/**
 * Response for {@link PipelineService#findPipelines(Identity, FindPipelinesRequest)}
 *
 * @author Vivek Pandey
 */
public final class FindPipelinesResponse {
    @JsonProperty("pipelines")
    public final List<Pipeline> pipelines;

    public FindPipelinesResponse(@JsonProperty("pipelines") List<Pipeline> pipelines) {
        this.pipelines = ImmutableList.copyOf(pipelines);
    }
}

