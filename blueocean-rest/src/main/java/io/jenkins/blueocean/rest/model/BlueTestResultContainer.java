package io.jenkins.blueocean.rest.model;

import io.jenkins.blueocean.rest.Reachable;
import io.jenkins.blueocean.rest.hal.Link;

public abstract class BlueTestResultContainer extends Container<BlueTestResult> {

    protected final Reachable parent;

    public BlueTestResultContainer(Reachable parent) {
        this.parent = parent;
    }

    @Override
    public Link getLink() {
        return parent.getLink().rel("tests");
    }
}
