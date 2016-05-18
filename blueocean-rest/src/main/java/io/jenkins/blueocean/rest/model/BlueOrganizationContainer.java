package io.jenkins.blueocean.rest.model;

import hudson.ExtensionPoint;
import io.jenkins.blueocean.rest.ApiRoutable;

/**
 * This is the head of the blue ocean API.
 *
 * @author Kohsuke Kawaguchi
 */
public abstract class BlueOrganizationContainer extends Container<BlueOrganization> implements ApiRoutable, ExtensionPoint {

    @Override
    public final String getUrlName() {
        return "organizations";
    }
}
