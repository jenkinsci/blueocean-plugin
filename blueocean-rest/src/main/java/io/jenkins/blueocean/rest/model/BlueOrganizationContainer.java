package io.jenkins.blueocean.rest.model;

import hudson.ExtensionPoint;
import io.jenkins.blueocean.rest.ApiRoutable;
import io.jenkins.blueocean.rest.Navigable;

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

    /**
     * A set of users who belong to this organization.
     *
     * @return {@link BlueUserContainer}
     */
    @Navigable
    public abstract BlueUserContainer getUsers();
}
