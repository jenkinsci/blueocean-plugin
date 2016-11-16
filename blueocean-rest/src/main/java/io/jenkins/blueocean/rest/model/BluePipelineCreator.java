package io.jenkins.blueocean.rest.model;

import hudson.ExtensionList;
import hudson.ExtensionPoint;

/**
 * @author Vivek Pandey
 */
public abstract class BluePipelineCreator implements ExtensionPoint {

    /**
     * Id of the creator
     */
    public abstract String getId();

    public abstract Class<? extends BluePipelineCreateRequest> getType();

    public static ExtensionList<BluePipelineCreator> all(){
        return ExtensionList.lookup(BluePipelineCreator.class);
    }
}
