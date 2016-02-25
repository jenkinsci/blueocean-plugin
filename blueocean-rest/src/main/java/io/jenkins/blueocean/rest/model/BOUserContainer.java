package io.jenkins.blueocean.rest.model;

import hudson.ExtensionPoint;
import io.jenkins.blueocean.rest.ApiRoutable;

import java.util.Iterator;

/**
 * User API.
 *
 * @author Vivek Pandey
 * @author Kohsuke Kawaguchi
 */
public abstract class BOUserContainer extends Container<BOUser> implements ApiRoutable, ExtensionPoint {
    @Override
    public final String getUrlName() {
        return "users";
    }

    /**
     * Most {@link BOUserContainer}s will be unlikely to support iteration.
     */
    @Override
    public Iterator<BOUser> iterator() {
        throw new UnsupportedOperationException();
    }
}
