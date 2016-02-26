package io.jenkins.blueocean;

import hudson.Extension;
import hudson.ExtensionList;

/**
 * Root of Blue Ocean UI
 *
 * @author Kohsuke Kawaguchi
 */
@Extension
public class BlueOceanUI {
    /**
     * Exposes {@link RootRoutable}s to the URL space.
     */
    public RootRoutable getDynamic(String route) {
        for (RootRoutable r : ExtensionList.lookup(RootRoutable.class)) {
            if (r.getUrlName().equals(route))
                return r;
        }
        return null;
    }
}
