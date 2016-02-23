package io.jenkins.blueocean;

import hudson.ExtensionPoint;
import hudson.model.Action;

/**
 * @author Kohsuke Kawaguchi
 */
public interface Routable {
    /**
     * Follows the same convention as {@link Action#getUrlName()}
     */
    String getUrlName();
}
