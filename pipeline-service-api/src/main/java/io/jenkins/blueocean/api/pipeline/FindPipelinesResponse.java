package io.jenkins.blueocean.api.pipeline;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableList;
import io.jenkins.blueocean.api.pipeline.model.Pipeline;

import java.util.List;

/**
 * Response object for findPipelines() method
 *
 * @author Vivek Pandey
 */
final class FindPipelinesResponse {
    @JsonProperty("pipelines")
    public final List<Pipeline> pipelines;

    public FindPipelinesResponse(@JsonProperty("pipelines") List<Pipeline> pipelines) {
        this.pipelines = ImmutableList.copyOf(pipelines);
    }
}

