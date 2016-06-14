package io.jenkins.blueocean.rest.model;

import hudson.ExtensionPoint;
import io.jenkins.blueocean.rest.ApiRoutable;
import io.jenkins.blueocean.rest.Reachable;

/**
 * BluePipeline container
 *
 * @author Kohsuke Kawaguchi
 */
public abstract class BluePipelineContainer extends Container<BluePipeline> implements ApiRoutable, ExtensionPoint{
    private final Reachable parent;

    protected BluePipelineContainer(Reachable parent) {
        this.parent = parent;
    }

    @Override
    public String getUrlName() {
        return "pipelines";
    }
}
