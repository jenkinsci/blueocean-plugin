package io.jenkins.blueocean.rest.model;

import io.jenkins.blueocean.commons.stapler.TreeResponse;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.WebMethod;
import org.kohsuke.stapler.verb.GET;
import org.kohsuke.stapler.verb.PUT;

/**
 * SCM resource attached to a pipeline
 *
 * @author Vivek Pandey
 */
public abstract class BluePipelineScm extends Resource {
    /**
     * Gives content in scm attached to a pipeline.
     *
     * @param request load scm request
     *
     * @return scm file content
     */
    @WebMethod(name = "content")
    @GET
    @TreeResponse
    public abstract Object getContent(StaplerRequest request);

    /**
     * Save a file to this SCM repository attached to this pipeline. Creates a new one if it doesn't exist.
     *
     * @param request save content to scm request
     *
     * @return response specific to SCM specific file save response
     */
    @PUT
    @WebMethod(name="content")
    @TreeResponse
    public abstract Object saveContent(StaplerRequest request);
}
