package io.jenkins.blueocean.api.profile;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.annotation.Nonnull;

/**
 * {@link ProfileService#getUserDetails(GetUserDetailsRequest)} request.
 *
 * @author Vivek Pandey
 */
public final class GetUserDetailsRequest{
    @JsonProperty("user")
    public final String id;

    public GetUserDetailsRequest(@Nonnull @JsonProperty("user") String id) {
        this.id = id;
    }
}

