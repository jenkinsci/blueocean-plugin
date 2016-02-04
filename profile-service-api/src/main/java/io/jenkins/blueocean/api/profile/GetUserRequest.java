package io.jenkins.blueocean.api.profile;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.jenkins.blueocean.commons.Identity;

import javax.annotation.Nonnull;

/**
 * Request for {@link ProfileService#getUser(Identity, GetUserRequest)}
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
