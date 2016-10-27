package io.jenkins.blueocean.rest.model;

import hudson.ExtensionList;
import hudson.ExtensionPoint;
import io.jenkins.blueocean.rest.Reachable;

import java.io.IOException;
import java.util.Map;

/**
 * @author Vivek Pandey
 */
public abstract class BluePipelineCreatorFactory implements ExtensionPoint {
    /**
     * Create an instance of {@link BluePipeline} from the given request.
     *
     * Implementation of this factory should return null if it can't handle given request
     *
     * @param request request to create a pipeline
     *
     * @return created pipeline
     */
    public abstract BluePipeline create(Map<String,Object> request, Reachable parent) throws IOException;

    public static ExtensionList<BluePipelineCreatorFactory> all(){
        return ExtensionList.lookup(BluePipelineCreatorFactory.class);
    }
}
