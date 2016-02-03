package io.jenkins.blueocean.api.profile;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Request object for {@link ProfileService#findUsers(FindUsersRequest)} method
 *
 * @author Vivek Pandey
 */
public class FindUsersRequest {
    /** Organization name */
    @JsonProperty("organization")
    public final String organization;
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

    public FindUsersRequest(@Nonnull @JsonProperty("organization") String organization,
                            @Nullable @JsonProperty("start") Long start, @Nullable@JsonProperty("limit") Long limit) {
        this.organization = organization;
        this.start = start;
        this.limit = limit;
    }
}
