package io.jenkins.blueocean.blueocean_bitbucket_pipeline.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * @author Vivek Pandey
 */
public abstract class BbPage<T> {
    @JsonProperty("start")
    public abstract int getStart();

    @JsonProperty("limit")
    public abstract int getLimit();

    @JsonProperty("size")
    public abstract int getSize();

    @JsonProperty("values")
    public abstract List<T> getValues();

    @JsonProperty("isLastPage")
    public abstract boolean isLastPage();
}
