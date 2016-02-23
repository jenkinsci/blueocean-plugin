package io.jenkins.blueocean.api.authentication;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.jenkins.blueocean.security.Credentials;

public final class AuthenticateRequest {
    @JsonProperty("authType")
    public final String authType;
    @JsonProperty("credentials")
    public final Credentials credentials;

    public AuthenticateRequest(@JsonProperty("authType") String authType, @JsonProperty("credentials") Credentials credentials) {
        this.authType = authType;
        this.credentials = credentials;
    }
}
