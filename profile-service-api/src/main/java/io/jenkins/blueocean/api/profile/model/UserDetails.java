package io.jenkins.blueocean.api.profile.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.annotation.Nonnull;

/**
 * UserDetails provides all user information. It contains confidential information and
 * should only meant to be seen by trusted clients.
 *
 * @author Vivek Pandey
 */
public class UserDetails {
    /** User id */
    @JsonProperty("id")
    public final String id;

    /** user name */
    @JsonProperty("name")
    public final String name;

    /** user email */
    @JsonProperty("email")
    public final String email;

    /** User's OAuth token */
    @JsonProperty("accessToken")
    public final String accessToken;

    /** Authentication provider. Possible values github, google etc. */
    @JsonProperty("authProvider")
    public final String authProvider;

    public UserDetails(@Nonnull @JsonProperty("id")String id,
                       @Nonnull@JsonProperty("name")String name,
                       @Nonnull@JsonProperty("email")String email,
                       @Nonnull@JsonProperty("accessToken")String accessToken,
                       @Nonnull@JsonProperty("authProvider")String authProvider) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.accessToken = accessToken;
        this.authProvider = authProvider;
    }

    /** Gives User object */
    @JsonIgnore
    public User toUser(){
        return new User(id, name, email);
    }
}
