package io.jenkins.blueocean.api.pipeline;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableList;
import io.jenkins.blueocean.api.pipeline.model.Run;
import io.jenkins.blueocean.commons.Identity;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

/**
 * Response for {@link PipelineService#findPipelineRuns(Identity, FindPipelineRunsRequest)}
 *
 * @author Vivek Pandey
 */
public final class FindPipelineRunsResponse {
    /**
     * Previous index of pipeline run from total
     */
    @JsonProperty("previous")
    public final Long previous;

    /**
     * Next index of pipeline run from total
     */
    @JsonProperty("next")
    public final Long next;

    @JsonProperty("runs")
    public final List<Run> runs;

    public FindPipelineRunsResponse(@Nonnull @JsonProperty("runs") List<Run> runs,
                                    @Nullable @JsonProperty("previous") Long previous,
                                    @Nullable @JsonProperty("next") Long next) {
        this.runs = ImmutableList.copyOf(runs);
        this.previous = previous;
        this.next = next;
    }
}