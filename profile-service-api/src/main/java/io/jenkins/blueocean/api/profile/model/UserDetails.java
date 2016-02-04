package io.jenkins.blueocean.api.profile.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableMap;
import io.jenkins.blueocean.commons.LoginDetails;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Map;

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

    /**
     * Map of auth provider id to login details
     *
     * <pre>
     * <code>
     * "loginDetails": {
     *     "github":{"accessToken":"aaaa11111"},
     *     "google":{"accessToken":"aaaa22222", "refreshToken":"ababababab"}
     * }
     * </code>
     * </pre>
     *
     * @see LoginDetails
     */
    @JsonProperty("loginDetails")
    Map<String, LoginDetails> loginDetailsMap;


    public UserDetails(@Nonnull @JsonProperty("id")String id,
                       @Nonnull@JsonProperty("name")String name,
                       @Nonnull@JsonProperty("email")String email,
                       @Nonnull@JsonProperty("loginDetails")Map<String,LoginDetails> loginDetailsMap
                       ) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.loginDetailsMap = ImmutableMap.copyOf(loginDetailsMap);
    }

    /** Gives User object */
    @JsonIgnore
    public @Nonnull User toUser(){
        return new User(id, name, email);
    }

    /**
     * Gives {@link LoginDetails} for the given auth provider id
     *
     * @param authProviderId id of the auth provider. loginDetailsMap is keyed with this id.
     * @return  LoginDetails instance corresponding to the given id. Null if no such id found.
     */
    @JsonIgnore
    @SuppressWarnings("unchecked")
    @Nullable public LoginDetails getLoginDetails(@Nonnull String authProviderId){
        return  loginDetailsMap.get(authProviderId);
    }

}
