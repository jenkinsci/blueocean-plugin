package io.jenkins.blueocean.rest;

import hudson.ExtensionPoint;
import hudson.model.Action;
import io.jenkins.blueocean.Routable;

/**
 * Marks the REST API endpoints.
 *
 * @author Kohsuke Kawaguchi
 * @author Vivek Pandey
 */
public interface ApiRoutable extends Routable, ExtensionPoint {
    /**
     * See {@link Action#getUrlName()} for contract.
     */
    String getUrlName();

    /**
     * Tells if this {@link Routable} route is parent to this route
     */
    boolean isChildOf(Routable parent);
}
