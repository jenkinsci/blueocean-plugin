package io.jenkins.blueocean.blueocean_bitbucket_pipeline.server.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.jenkins.blueocean.blueocean_bitbucket_pipeline.model.BbPage;

import java.util.List;

/**
 * @author Vivek Pandey
 */
public class BbServerPage<T> extends BbPage<T> {
    private final int start;
    private final int limit;
    private final int size;
    private final List<T> values;
    private final boolean isLastPage;

    @JsonCreator
    public BbServerPage(@JsonProperty("start") int start, @JsonProperty("limit")int limit, @JsonProperty("size")int size,
                        @JsonProperty("values")List<T> values, @JsonProperty("isLastPage")boolean isLastPage) {
        this.start = start;
        this.limit = limit;
        this.size = size;
        this.values = values;
        this.isLastPage = isLastPage;
    }

    @Override
    @JsonProperty("start")
    public int getStart() {
        return start;
    }

    @Override
    @JsonProperty("limit")
    public int getLimit() {
        return limit;
    }

    @Override
    @JsonProperty("size")
    public int getSize() {
        return size;
    }

    @Override
    @JsonProperty("values")
    public List<T> getValues() {
        return values;
    }

    @Override
    @JsonProperty("isLastPage")
    public boolean isLastPage() {
        return isLastPage;
    }
}
