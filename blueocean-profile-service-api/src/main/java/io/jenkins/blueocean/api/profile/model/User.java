package io.jenkins.blueocean.api.profile.model;

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
    /** The id of the user */
    @JsonProperty("id")
    public final String id;

    /** The name of the user e.g. John Smith */
    @JsonProperty("fullName")
    public final String fullName;


    public User(@Nonnull @JsonProperty("id")String id,
                @Nonnull @JsonProperty("fullName")String fullName) {
        this.id = id;
        this.fullName = fullName;
    }
}
