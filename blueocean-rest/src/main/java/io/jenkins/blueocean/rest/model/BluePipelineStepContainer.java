package io.jenkins.blueocean.rest.model;

import io.jenkins.blueocean.rest.hal.Link;

/**
 * Container for pipeline step resource
 *
 * @author Vivek Pandey
 */
public abstract class BluePipelineStepContainer extends Container<BluePipelineStep> {
    protected final Link self;

    public BluePipelineStepContainer(Link parent) {
        this.self = parent.rel("steps");
    }

    @Override
    public Link getLink() {
        return self;
    }
}
