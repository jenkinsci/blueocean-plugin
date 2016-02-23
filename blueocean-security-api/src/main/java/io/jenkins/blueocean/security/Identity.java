package io.jenkins.blueocean.security;

import javax.annotation.Nonnull;
import java.security.Principal;

/**
 * Represents authenticated user principal
 *
 * @author Vivek Pandey
 */
public final class Identity implements Principal {

    /**
     * Anonymous user's identity. Typically indicates context where there is
     * no logged in user.
     */
    public static final Identity ANONYMOUS = new Identity("anonymous");

    /**
     * The root system user
     */
    public static final Identity ROOT = new Identity("root");

    public final String user;

    public Identity(@Nonnull String user) {
        this.user = user;
    }

    @Override
    public String getName() {
        return user;
    }

    public boolean isAnonymous() {
        return this.equals(ANONYMOUS);
    }
}
