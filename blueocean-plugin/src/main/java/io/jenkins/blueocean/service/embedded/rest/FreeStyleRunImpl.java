package io.jenkins.blueocean.service.embedded.rest;

import hudson.model.Run;

/**
 * FreeStyleRunImpl can add it's own element here
 *
 * @author Vivek Pandey
 */
public class FreeStyleRunImpl extends AbstractRunImpl {

    public FreeStyleRunImpl(Run run) {
        super(run);
    }
}
