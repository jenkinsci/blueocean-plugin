package io.jenkins.blueocean.api.profile;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.jenkins.blueocean.api.profile.model.User;

import javax.annotation.Nullable;

/**
 * {@link ProfileService#getUser(GetUserRequest)} response.
 *
 * @author Vivek Pandey
 */
public final class GetUserResponse{
    @JsonProperty("user")
    public final User user;

    public GetUserResponse(@Nullable @JsonProperty("user")User user) {
        this.user = user;
    }
}
