package io.jenkins.blueocean.api.profile;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.jenkins.blueocean.api.profile.model.UserDetails;

import javax.annotation.Nullable;

/**
 * {@link ProfileService#getUserDetails(GetUserDetailsRequest)} response.
 *
 * @author Vivek Pandey
 */
public final class GetUserDetailsResponse{
    @JsonProperty("userDetails")
    public final UserDetails userDetails;

    public GetUserDetailsResponse(@Nullable @JsonProperty("userDetails")UserDetails userDetails) {
        this.userDetails = userDetails;
    }
}

