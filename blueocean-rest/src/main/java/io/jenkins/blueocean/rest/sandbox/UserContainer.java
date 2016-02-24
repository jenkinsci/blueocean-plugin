package io.jenkins.blueocean.rest.sandbox;

import hudson.ExtensionPoint;
import io.jenkins.blueocean.rest.ApiRoutable;

import java.util.Iterator;

/**
 * User API.
 *
 * @author Vivek Pandey
 * @author Kohsuke Kawaguchi
 */
public abstract class UserContainer extends Container<User> implements ApiRoutable, ExtensionPoint {
    @Override
    public final String getUrlName() {
        return "users";
    }

    /**
     * Most {@link UserContainer}s will be unlikely to support iteration.
     */
    @Override
    public Iterator<User> iterator() {
        throw new UnsupportedOperationException();
    }
}
