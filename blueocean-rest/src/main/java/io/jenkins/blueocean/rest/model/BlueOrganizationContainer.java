package io.jenkins.blueocean.rest.model;

import hudson.ExtensionList;
import hudson.ExtensionPoint;
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

    public static BlueOrganization getBlueOrganization(){
        for(BlueOrganization action: ExtensionList.lookup(BlueOrganization.class)){
            return action;
        }
        return null;
    }
}
