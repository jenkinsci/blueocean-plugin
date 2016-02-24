package io.jenkins.blueocean.api.profile;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.jenkins.blueocean.api.profile.model.UserDetails;

public class CreateUserResponse {
    @JsonProperty("userDetails")
    public final UserDetails userDetails;

    public CreateUserResponse(@JsonProperty("userDetails") UserDetails userDetails) {
        this.userDetails = userDetails;
    }
}
