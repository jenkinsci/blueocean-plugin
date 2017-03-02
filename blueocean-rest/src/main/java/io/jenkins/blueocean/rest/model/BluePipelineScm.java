package io.jenkins.blueocean.rest.model;

import io.jenkins.blueocean.commons.stapler.TreeResponse;
import org.kohsuke.stapler.QueryParameter;
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
     * @param path path scm resource, e.g. Jenkinsfile or config/Jenkinsfile
     * @param type type type of content. Default is file
     * @return
     */
    @WebMethod(name = "content")
    @GET
    @TreeResponse
    public abstract Object getContent(@QueryParameter(value = "path", fixEmpty = true) String path,
                                      @QueryParameter(value = "type", fixEmpty = true) String type);

    /**
     * Save a file to this SCM repository attached to this pipeline. Creates a new one if it doesn't exist.
     *
     * @param request file content to store in SCM.
     *
     * @return response specific to SCM specific file save response
     */
    @PUT
    @WebMethod(name="content")
    @TreeResponse
    public abstract Object saveContent(StaplerRequest request);
}
