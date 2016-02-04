package io.jenkins.blueocean.api.profile;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.jenkins.blueocean.api.profile.model.User;
import io.jenkins.blueocean.commons.Identity;

import javax.annotation.Nullable;

/**
 * Response for {@link ProfileService#getUser(Identity, GetUserRequest)}
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
