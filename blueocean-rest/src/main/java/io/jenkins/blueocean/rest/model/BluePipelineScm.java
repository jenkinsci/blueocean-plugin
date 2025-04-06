package io.jenkins.blueocean.rest.model;

import io.jenkins.blueocean.commons.stapler.TreeResponse;
import io.jenkins.blueocean.rest.annotation.Capability;
import org.kohsuke.stapler.StaplerRequest2;
import org.kohsuke.stapler.WebMethod;
import org.kohsuke.stapler.verb.GET;
import org.kohsuke.stapler.verb.PUT;

import static io.jenkins.blueocean.rest.model.KnownCapabilities.BLUE_SCM;

/**
 * SCM resource attached to a pipeline
 *
 * @author Vivek Pandey
 */
@Capability(BLUE_SCM)
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
    public abstract Object getContent(StaplerRequest2 request);

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
    public abstract Object saveContent(StaplerRequest2 request);
}
