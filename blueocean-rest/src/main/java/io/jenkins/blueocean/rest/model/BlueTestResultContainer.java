package io.jenkins.blueocean.rest.model;

import io.jenkins.blueocean.rest.hal.Link;

public abstract class BlueTestResultContainer extends Container<BlueTestResult> {

    protected final BlueRun parent;

    public BlueTestResultContainer(BlueRun parent) {
        this.parent = parent;
    }

    @Override
    public Link getLink() {
        return parent.getLink().rel("tests");
    }
}
