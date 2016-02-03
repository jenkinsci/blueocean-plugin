package io.jenkins.blueocean.api.profile;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.jenkins.blueocean.commons.Identity;

import javax.annotation.Nonnull;

/**
 * Request for {@link ProfileService#getUserDetails(Identity, GetUserDetailsRequest)}
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

