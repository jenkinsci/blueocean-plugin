package io.jenkins.blueocean.api.authentication;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.jenkins.blueocean.api.profile.model.UserDetails;
import io.jenkins.blueocean.security.UserPrototype;

import javax.annotation.Nullable;

public final class AuthenticateResponse {
    /** If the user does not exist this object can be used to create the user */
    @Nullable
    @JsonProperty("userPrototype")
    public final UserPrototype userPrototype;

    /** The UserDetails of the user being authenticated. Not null if the user exists */
    @Nullable
    @JsonProperty("userDetails")
    public final UserDetails userDetails;

    public AuthenticateResponse(
        @JsonProperty("userPrototype") UserPrototype userPrototype,
        @JsonProperty("userDetails") UserDetails userDetails) {
        this.userPrototype = userPrototype;
        this.userDetails = userDetails;
    }
}
