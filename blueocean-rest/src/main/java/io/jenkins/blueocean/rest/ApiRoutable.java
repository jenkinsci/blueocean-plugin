package io.jenkins.blueocean.rest;

import hudson.ExtensionPoint;
import io.jenkins.blueocean.Routable;

/**
 * Marks the REST API endpoints that are exposed by {@link ApiHead}
 *
 * @author Kohsuke Kawaguchi
 * @author Vivek Pandey
 */
public interface ApiRoutable extends Routable, ExtensionPoint {
    /**
     * See Action.getUrlName() for contract.
     */
    String getUrlName();
}
