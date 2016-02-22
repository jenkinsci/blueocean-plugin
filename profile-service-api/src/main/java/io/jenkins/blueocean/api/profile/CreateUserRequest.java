package io.jenkins.blueocean.api.profile;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.jenkins.blueocean.security.Credentials;
import io.jenkins.blueocean.security.UserPrototype;

public final class CreateUserRequest {
    @JsonProperty("authType")
    public final String authType;
    @JsonProperty("fullName")
    public final String fullName;
    @JsonProperty("email")
    public final String email;
    @JsonProperty("credentials")
    public final Credentials credentials;

    public CreateUserRequest(@JsonProperty("authType") String authType,
                             @JsonProperty("fullName") String fullName,
                             @JsonProperty("email") String email,
                             @JsonProperty("credentials") Credentials credentials) {
        this.authType = authType;
        this.fullName = fullName;
        this.email = email;
        this.credentials = credentials;
    }

    /** Create a new user from a UserPrototype */
    public static CreateUserRequest fromUserPrototype(UserPrototype userPrototype, String authType, Credentials credentials) {
        return new CreateUserRequest(authType, userPrototype.fullName, userPrototype.email, credentials);
    }
}
