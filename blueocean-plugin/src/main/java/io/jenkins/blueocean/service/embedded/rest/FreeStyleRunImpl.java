package io.jenkins.blueocean.service.embedded.rest;

import hudson.model.FreeStyleBuild;
import hudson.model.Run;

/**
 * FreeStyleRunImpl can add it's own element here
 *
 * @author Vivek Pandey
 */
public class FreeStyleRunImpl extends AbstractRunImpl {

    private final FreeStyleBuild run;
    public FreeStyleRunImpl(FreeStyleBuild run) {
        this.run = run;
    }

    @Override
    protected Run getRun() {
        return null;
    }

    @Override
    public String getBranch() {
        return null;
    }

    @Override
    public String getCommitId() {
        return null;
    }
}
