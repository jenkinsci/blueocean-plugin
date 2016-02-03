package io.jenkins.blueocean.api.pipeline;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableList;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

/**
 * Request for findPipelineRuns() method
 *
 * @author Vivek Pandey
 */
final class FindPipelineRunsRequest {
    public static final long START_DEFAULT = 1;

    //TODO: we should change it to some reasonalbe value
    public static final long LIMIT_DEFAULT = -1;

    @JsonProperty("organization")
    public final String organization;

    @JsonProperty("pipeline")
    public final String pipeline;

    @JsonProperty("latestOnly")
    public final boolean latestOnly;

    @JsonProperty("branches")
    public final List<String> branches;

    /**
     * start is the starting pipeline result number request from total
     */
    @JsonProperty("start")
    public final Long start;


    /**
     * total number of pipeline run records requested
     */
    @JsonProperty("limit")
    public final Long limit;

    public FindPipelineRunsRequest(@Nonnull @JsonProperty("organization") String organization,
                                   @Nullable @JsonProperty("pipeline") String pipeline,
                                   @Nullable @JsonProperty("latestOnly") Boolean latestOnly,
                                   @Nullable @JsonProperty("branches") List<String> branches,
                                   @Nullable @JsonProperty("start") Long start,
                                   @Nullable @JsonProperty("limit") Long limit
    ) {
        this.organization = organization;
        this.pipeline = pipeline;
        this.latestOnly = (latestOnly != null) ? latestOnly : false;
        this.branches = (branches != null) ? ImmutableList.copyOf(branches) : null;
        this.start = start;
        this.limit = limit;

    }
}
