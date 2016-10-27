package io.jenkins.blueocean.rest.model;

import hudson.ExtensionList;
import hudson.ExtensionPoint;
import io.jenkins.blueocean.rest.Reachable;

import java.io.IOException;

/**
 * @author Vivek Pandey
 */
public abstract class BluePipelineCreator implements ExtensionPoint {

    /**
     * Id of the creator
     */
    public abstract String getId();

    /**
     * Create an instance of {@link BluePipeline} from the given request.
     *
     * Implementation of this factory should return null if it can't handle given request
     *
     * @param request request to create a pipeline
     *
     * @return created pipeline
     */
    public abstract BluePipeline create(BluePipelineCreateRequest request, Reachable parent) throws IOException;

    public static ExtensionList<BluePipelineCreator> all(){
        return ExtensionList.lookup(BluePipelineCreator.class);
    }
}
