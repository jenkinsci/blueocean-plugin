package io.jenkins.blueocean;

import hudson.ExtensionList;
import hudson.model.UsageStatistics;
import jenkins.model.Jenkins;

/**
 * Root of Blue Ocean UI
 *
 * @author Kohsuke Kawaguchi
 */
public class BlueOceanUI {
    private final String urlBase;

    public BlueOceanUI(String rootPath) {
        this.urlBase = rootPath;
    }

    /**
     * Exposes {@link RootRoutable}s to the URL space. Returns <code>this</code> if none found, allowing the UI to
     * resolve routes. This also has the side effect that we won't be able to generate 404s for any URL that *might*
     * resolve to a valid UI route. If and when we implement server-side rendering of initial state or to solidify the
     * routes on the back-end for real 404s, we'll need to complicate this behaviour :D
     */
    public Object getDynamic(String route) {
        for (RootRoutable r : ExtensionList.lookup(RootRoutable.class)) {
            if (r.getUrlName().equals(route))
                return r;
        }
        return this;
    }

    /**
     * The base of all BlueOcean URLs (underneath wherever Jenkins itself is deployed).
     */
    public String getUrlBase() {
        return urlBase;
    }

    /**
     * @return True if jenkins reports usage statistics.
     */
    public boolean includeRollbar() {
        return false;
//        return Jenkins.getInstance().isUsageStatisticsCollected() && !UsageStatistics.DISABLED;
    }

    /**
     * @return Version on the plugin e.g 1.0-SNAPSHOT (private-f9a14d3e-jenkins)
     */
    public String getPluginVersion() {
        return Jenkins.getInstance().getPlugin("blueocean-web").getWrapper().getVersion();
    }
}
