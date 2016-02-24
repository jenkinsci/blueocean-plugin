package io.jenkins.blueocean.api.pipeline.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableMap;

import javax.annotation.Nonnull;
import java.util.Map;

/**
 * Marker interface that describes build result
 *
 * @author Vivek Pandey
 * @see PipelineResult
 * @see JobResult
 * @see Run
 */
public class Result{
    @JsonProperty("type")
    public final String type;

    @JsonProperty("data")
    public final Map<String, ?> data;

    public Result(@Nonnull @JsonProperty("type") String type, @Nonnull @JsonProperty("data") Map<String, ?> data) {
        this.type = type;
        this.data = ImmutableMap.copyOf(data);
    }
}
