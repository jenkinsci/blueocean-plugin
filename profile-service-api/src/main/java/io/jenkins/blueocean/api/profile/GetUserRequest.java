package io.jenkins.blueocean.api.profile;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.annotation.Nonnull;

/**
 * {@link ProfileService#getUser(GetUserRequest)} request.
 *
 * @author Vivek Pandey
 */
public final class GetUserRequest{
    @JsonProperty("id")
    public final String id;

    public GetUserRequest(@Nonnull @JsonProperty("id") String id) {
        this.id = id;
    }
}
