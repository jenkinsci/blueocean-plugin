package io.jenkins.blueocean.rest.sandbox;

import io.jenkins.blueocean.security.Credentials;
import org.kohsuke.stapler.export.Exported;

import java.util.List;

/**
 * API endpoint for a user
 *
 * @author Kohsuke Kawaguchi
 * @author Vivek Pandey
 */
public abstract class User extends Resource {
    /**
     * The id of the user
     */
    @Exported
    public abstract String getId();

    /**
     * The name of the user e.g. John Smith
     */
    @Exported
    public abstract String getFullName();

    /**
     * Email address of this user.
     */
    @Exported
    // restricted to authorized users only
    public abstract String getEmail();

    @Exported(inline=true)
    // restricted to authorized users only
    public abstract List<Credentials> getCredentials();

}
