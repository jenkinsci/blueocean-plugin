package io.jenkins.blueocean.rest.model;

import io.jenkins.blueocean.rest.Reachable;

import javax.annotation.CheckForNull;
import java.io.IOException;

/**
 * Pipeline create request.
 *
 * @author Vivek Pandey
 */
public abstract class BluePipelineCreateRequest {

    private String name;

    /** Name of the pipeline */
    public @CheckForNull String getName(){
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * Create an instance of {@link BluePipeline} from the given request.
     *
     * @return created pipeline
     */
    public abstract @CheckForNull BluePipeline create(Reachable parent) throws IOException;
}
