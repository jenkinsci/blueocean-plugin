package io.jenkins.blueocean.rest.model;

import org.kohsuke.stapler.StaplerRequest;

import javax.annotation.CheckForNull;
import java.io.IOException;

/**
 * BluePipeline update request
 *
 * @author Vivek Pandey
 * @see BluePipeline#update(StaplerRequest)
 */
public abstract class BluePipelineUpdateRequest {
    /**
     * Update an instance of {@link BluePipeline}
     *
     * @return updated pipeline. Null if update failed or pipeline instance is not the right type
     */
    public abstract @CheckForNull BluePipeline update(BluePipeline pipeline) throws IOException;
}
