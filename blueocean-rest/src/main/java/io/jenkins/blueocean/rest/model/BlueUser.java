package io.jenkins.blueocean.rest.model;

import io.jenkins.blueocean.rest.Navigable;
import io.jenkins.blueocean.rest.annotation.Capability;
import org.kohsuke.stapler.export.Exported;

import static io.jenkins.blueocean.rest.model.KnownCapabilities.BLUE_USER;

/**
 * API endpoint for a user
 *
 * @author Kohsuke Kawaguchi
 * @author Vivek Pandey
 */
@Capability(BLUE_USER)
public abstract class BlueUser extends Resource {
    public static final String ID="id";
    public static final String FULL_NAME="fullName";
    public static final String EMAIL="email";
    public static final String FAVORITES = "favorites";
    private static final String PERMISSION = "permission";
    private static final String AVATAR = "avatar";

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

    @Exported(name = AVATAR)
    public abstract String getAvatar();

    @Navigable
    public abstract BlueFavoriteContainer getFavorites();

    @Exported(name = PERMISSION, inline = true)
    public abstract BlueUserPermission getPermission();
}
