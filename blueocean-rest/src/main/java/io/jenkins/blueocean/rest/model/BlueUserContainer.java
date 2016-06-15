package io.jenkins.blueocean.rest.model;

import hudson.ExtensionPoint;
import io.jenkins.blueocean.rest.ApiHead;
import io.jenkins.blueocean.rest.ApiRoutable;
import io.jenkins.blueocean.rest.Reachable;
import io.jenkins.blueocean.rest.hal.Link;

import java.util.Iterator;

/**
 * User API.
 *
 * @author Vivek Pandey
 * @author Kohsuke Kawaguchi
 */
public abstract class BlueUserContainer extends Container<BlueUser> implements ApiRoutable, ExtensionPoint {
    private final Reachable parent;

    protected BlueUserContainer(Reachable parent) {
        this.parent = parent;
    }


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

    @Override
    public Link getLink() {
        if(parent!=null) {
            return parent.getLink().rel(getUrlName());
        }
        return ApiHead.INSTANCE().getLink().rel(getUrlName());
    }
}
