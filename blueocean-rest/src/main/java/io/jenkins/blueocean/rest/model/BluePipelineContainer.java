package io.jenkins.blueocean.rest.model;

import hudson.ExtensionPoint;
import io.jenkins.blueocean.rest.ApiRoutable;

/**
 * BluePipeline container
 *
 * @author Kohsuke Kawaguchi
 */
public abstract class BluePipelineContainer extends Container<BluePipeline> implements ApiRoutable, ExtensionPoint{
    protected BluePipelineContainer() {
    }

    @Override
    public String getUrlName() {
        return "pipelines";
    }
}
