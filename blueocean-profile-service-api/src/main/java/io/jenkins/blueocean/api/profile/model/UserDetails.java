package io.jenkins.blueocean.api.profile.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableList;
import io.jenkins.blueocean.security.Credentials;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Set;

/**
 * UserDetails provides all user information.
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
}
