package io.jenkins.blueocean.api.profile.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import io.jenkins.blueocean.security.Credentials;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Set;

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
    @JsonProperty("fullName")
    public final String fullName;

    /** user email */
    @JsonProperty("email")
    public final String email;

    @JsonProperty("credentials")
    List<Credentials> credentials;


    public UserDetails(@Nonnull @JsonProperty("id") String id,
                       @Nonnull @JsonProperty("fullName") String fullName,
                       @Nullable @JsonProperty("email") String email,
                       @Nonnull @JsonProperty("credentials") Set<Credentials> credentials
                       ) {
        this.id = id;
        this.fullName = fullName;
        this.email = email;
        this.credentials = ImmutableList.copyOf(credentials);
    }

    /** Gives User object */
    @JsonIgnore
    public @Nonnull User toUser(){
        return new User(id, fullName);
    }

    /**
     * @param loginDetailsClass of the login details implementation to look up
     * @return  Credentials instance corresponding to the given Credentials class. Null if no such id found.
     */
    @JsonIgnore
    @SuppressWarnings("unchecked")
    @Nullable public <T extends Credentials> Credentials getLoginDetails(Class<T> loginDetailsClass) {
        return Maps.uniqueIndex(this.credentials, new Function<Credentials, Class<Credentials>>() {
            @Override
            public Class<Credentials> apply(@Nullable Credentials credentials) {
                if(credentials != null) {
                    return (Class<Credentials>) credentials.getClass();
                }
                return null;
            }
        }).get(loginDetailsClass);
    }
}
