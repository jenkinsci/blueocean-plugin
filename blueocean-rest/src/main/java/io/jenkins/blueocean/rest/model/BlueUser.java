package io.jenkins.blueocean.rest.model;

import org.kohsuke.stapler.export.Exported;

/**
 * API endpoint for a user
 *
 * @author Kohsuke Kawaguchi
 * @author Vivek Pandey
 */
public abstract class BlueUser extends Resource {
    public static final String ID="id";
    public static final String FULL_NAME="fullName";
    public static final String EMAIL="email";
    public static final String FAVORITES = "favorites";

    /**
     * @return The id of the user
     */
    @Exported(name=ID)
    public abstract String getId();

    /**
     * @return The name of the user e.g. John Smith
     */
    @Exported(name = FULL_NAME)
    public abstract String getFullName();

    /**
     * @return Email address of this user.
     */
    @Exported(name = EMAIL)
    // restricted to authorized users only
    public abstract String getEmail();

    public abstract BlueFavoriteContainer getFavorites();

}
