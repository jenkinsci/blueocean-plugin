package io.jenkins.blueocean.api.pipeline.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableMap;

import javax.annotation.Nonnull;
import java.util.Map;

/**
 * Describes Job result
 *
 * @author Vivek Pandey
 */
public class JobResult{

    @JsonProperty("data")
    private final Map<String, String> jobResultData;

    public JobResult(@Nonnull @JsonProperty("data") Map<String, String> jobResultData) {
        this.jobResultData = jobResultData;
    }


    public String getType() {
        return "job";
    }


    public Map<String,String> getData() {
        return ImmutableMap.copyOf(jobResultData);
    }
}
