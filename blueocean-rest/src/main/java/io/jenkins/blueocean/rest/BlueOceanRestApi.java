package io.jenkins.blueocean.rest;

import hudson.ExtensionPoint;
import hudson.model.Action;

/**
 * Marks the REST API endpoints that are exposed by {@link ApiHead}
 *
 * @author Kohsuke Kawaguchi
 */
public interface BlueOceanRestApi extends ExtensionPoint {
    /**
     * See {@link Action#getUrlName()} for contract.
     */
    String getUrlName();
}
