package io.jenkins.blueocean.api.pipeline.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.annotation.Nonnull;
import java.util.Map;

/**
 * Abstract to describe Pipeline/workflow build result
 *
 * @author Vivek Pandey
 */
public class PipelineResult{
    @JsonProperty("data")
    private final Map<String, Object> pipelineResultData;

    public PipelineResult(@Nonnull @JsonProperty("data") Map<String, Object> data) {
        this.pipelineResultData = data;
    }


    public String getType() {
        return null;
    }



    public Map<String, Object> getData() {
        return pipelineResultData;
    }
}
