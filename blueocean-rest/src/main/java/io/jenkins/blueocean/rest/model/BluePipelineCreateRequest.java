package io.jenkins.blueocean.rest.model;

import hudson.ExtensionList;
import io.jenkins.blueocean.rest.Reachable;
import org.apache.tools.ant.ExtensionPoint;

import javax.annotation.CheckForNull;
import java.io.IOException;

/**
 * Pipeline create request.
 *
 * @author Vivek Pandey
 */
public abstract class BluePipelineCreateRequest extends ExtensionPoint{

    /** Name of the pipeline */
    public abstract @CheckForNull String getName();

    /**
     * Create an instance of {@link BluePipeline} from the given request.
     *
     * @return created pipeline
     */
    public abstract @CheckForNull BluePipeline create(Reachable parent) throws IOException;

    public static ExtensionList<BluePipelineCreateRequest> all(){
        return ExtensionList.lookup(BluePipelineCreateRequest.class);
    }
}
