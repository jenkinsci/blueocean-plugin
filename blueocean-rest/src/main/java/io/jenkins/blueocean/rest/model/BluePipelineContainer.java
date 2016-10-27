package io.jenkins.blueocean.rest.model;

import io.jenkins.blueocean.commons.stapler.JsonBody;
import io.jenkins.blueocean.commons.stapler.TreeResponse;
import org.kohsuke.stapler.WebMethod;
import org.kohsuke.stapler.verb.POST;

import java.io.IOException;
import java.util.Map;

/**
 * BluePipeline container
 *
 * @author Kohsuke Kawaguchi
 */
public abstract class BluePipelineContainer extends Container<BluePipeline>{

    /**
     * Create new pipeline
     *
     * @param request
     * @return
     */
    @POST
    @WebMethod(name = "")
    @TreeResponse
    public abstract  BluePipeline create(@JsonBody Map<String,Object> request) throws IOException;
}
