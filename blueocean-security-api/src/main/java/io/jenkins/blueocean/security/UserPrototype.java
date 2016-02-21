package io.jenkins.blueocean.security;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.annotation.Nullable;

/** Represents the minimal amount of information needed to create a user */
public final class UserPrototype {
    @JsonProperty("fullName")
    @Nullable
    public final String fullName;
    @JsonProperty("email")
    public final String email;

    public UserPrototype(String fullName, String email) {
        this.fullName = fullName;
        this.email = email;
    }
}
