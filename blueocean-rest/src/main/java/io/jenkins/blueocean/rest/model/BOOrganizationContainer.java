package io.jenkins.blueocean.rest.model;

import hudson.ExtensionPoint;
import io.jenkins.blueocean.rest.ApiRoutable;

/**
 * This is the head of the blue ocean API.
 *
 * @author Kohsuke Kawaguchi
 */
public abstract class BOOrganizationContainer extends Container<BOOrganization> implements ApiRoutable, ExtensionPoint {

    @Override
    public final String getUrlName() {
        return "organizations";
    }

    /**
     * A set of users who belong to this organization.
     *
     * @return {@link BOUserContainer}
     */
    public abstract BOUserContainer getUsers();
}
