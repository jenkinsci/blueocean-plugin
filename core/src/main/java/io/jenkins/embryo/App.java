package io.jenkins.embryo;

import hudson.ExtensionPoint;

/**
 * Thre must be a sole implementation of this, which is the root of the application.
 *
 * @author Kohsuke Kawaguchi
 */
public abstract class App implements ExtensionPoint {
}
