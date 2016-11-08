package io.jenkins.blueocean.rest.model;

import hudson.ExtensionPoint;
import io.jenkins.blueocean.Routable;
import io.jenkins.blueocean.rest.ApiHead;
import io.jenkins.blueocean.rest.ApiRoutable;

/**
 * Container of BlueOcean {@link BlueOrganization}s
 *
 * @author Kohsuke Kawaguchi
 * @author Vivek Pandey
 */
public abstract class BlueOrganizationContainer extends Container<BlueOrganization> implements ApiRoutable, ExtensionPoint {

    @Override
    public final String getUrlName() {
        return "organizations";
    }

    @Override
    public boolean isChildOf(Routable ancestor) {
        return ancestor instanceof ApiHead;
    }
}
