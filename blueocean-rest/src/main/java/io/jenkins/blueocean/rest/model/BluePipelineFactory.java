package io.jenkins.blueocean.rest.model;

import hudson.ExtensionList;
import hudson.ExtensionPoint;
import hudson.model.Item;
import io.jenkins.blueocean.rest.Reachable;

/**
 * Factory that gives instance of {@link BluePipeline}
 *
 * It's useful for example in cases where a plugin that has custom project and they want to serve
 * extra meta-data thru BluePipeline, would provide implementation of their BluePipeline and and implementation
 * of BluePipelineFactory.
 *
 * @author Vivek Pandey
 */
public abstract class BluePipelineFactory implements ExtensionPoint {
    public abstract BluePipeline getPipeline(Item item, Reachable parent);

    public static ExtensionList<BluePipelineFactory> all(){
        return ExtensionList.lookup(BluePipelineFactory.class);
    }
}
