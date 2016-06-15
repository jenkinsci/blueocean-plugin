package io.jenkins.blueocean.rest.model;

import hudson.ExtensionPoint;
import io.jenkins.blueocean.rest.ApiHead;
import io.jenkins.blueocean.rest.ApiRoutable;
import io.jenkins.blueocean.rest.hal.Link;

/**
 * BluePipeline container
 *
 * @author Kohsuke Kawaguchi
 */
public abstract class BluePipelineContainer extends Container<BluePipeline> implements ApiRoutable, ExtensionPoint{
    protected final Link parent;

    protected BluePipelineContainer(Link parent) {
        this.parent = parent;
    }

    @Override
    public String getUrlName() {
        return "pipelines";
    }

    @Override
    public Link getLink() {
        if(parent!=null) {
            return parent.rel(getUrlName());
        }
        return ApiHead.INSTANCE().getLink().rel(getUrlName());
    }
}
