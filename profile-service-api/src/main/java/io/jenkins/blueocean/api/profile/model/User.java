package io.jenkins.blueocean.api.profile.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.annotation.Nonnull;

/**
 * User's public  information. It captures minimal information to represent a user. No confidential information
 * of the user is stored in this class.
 *
 * For all information of the user including confidential information
 * see {@link UserDetails}.
 *
 * @author Vivek Pandey
 * @see UserDetails
 */
public class User {
    @JsonProperty("id")
    public final String id;

    @JsonProperty("name")
    public final String name;

    /** Email at this point is kept private as there is no current use case
     * to show it in public
     */
    @JsonIgnore
    private final String email;

    public User(@Nonnull @JsonProperty("id")String id,
                @Nonnull @JsonProperty("name")String name,
                @Nonnull @JsonProperty("email")String email) {
        this.id = id;
        this.name = name;
        this.email = email;
    }
}
