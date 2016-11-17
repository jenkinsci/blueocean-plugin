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
public abstract class BlueUserContainer extends Container<BlueUser> implements ApiRoutable, ExtensionPoint {

    @Override
    public final String getUrlName() {
        return "users";
    }

    /**
     * Most {@link BlueUserContainer}s will be unlikely to support iteration.
     */
    @Override
    public Iterator<BlueUser> iterator() {
        throw new UnsupportedOperationException();
    }
}
