package io.jenkins.blueocean.rest.model;

import io.jenkins.blueocean.rest.hal.Link;

/**
 * @author Vivek Pandey
 */
public abstract class BluePipelineNodeContainer extends Container<BluePipelineNode>{
    protected final Link self;

    public BluePipelineNodeContainer(Link parent) {
        this.self = parent.rel("nodes");
    }
}
