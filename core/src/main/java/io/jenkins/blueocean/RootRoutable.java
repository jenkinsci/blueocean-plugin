package io.jenkins.blueocean;

import hudson.ExtensionPoint;

/**
 * Additional {@link Routable}s that augment {@link BlueOceanUI} root object.
 *
 * @author Kohsuke Kawaguchi
 */
public interface RootRoutable extends Routable, ExtensionPoint {
}
