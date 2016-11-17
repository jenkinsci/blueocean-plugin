package io.jenkins.blueocean.rest.model;

import io.jenkins.blueocean.commons.ServiceException;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.WebMethod;
import org.kohsuke.stapler.json.JsonBody;
import org.kohsuke.stapler.verb.POST;

import java.io.IOException;

/**
 * BluePipeline container
 *
 * @author Kohsuke Kawaguchi
 */
public abstract class BluePipelineContainer extends Container<BluePipeline>{

    /**
     * Create new pipeline.
     *
     * @param body {@link BluePipelineCreateRequest} request object
     * @return {@link CreateResponse} response
     */
    @POST
    @WebMethod(name = "")
    public  CreateResponse create(@JsonBody JSONObject body, StaplerRequest staplerRequest) throws IOException{
        if(body.get("$class") == null){
            throw new ServiceException.BadRequestExpception("$class is required element");
        }
        BluePipelineCreateRequest request = staplerRequest.bindJSON(BluePipelineCreateRequest.class, body);
        return create(request);
    }

    public CreateResponse create(BluePipelineCreateRequest request) throws IOException {
        if(request.getName() == null){
            throw new ServiceException.BadRequestExpception("name is required element");
        }
        BluePipeline pipeline = request.create(this);
        if(pipeline == null){
            throw new ServiceException.UnexpectedErrorException("Failed to create pipeline: "+request.getName());
        }
        return new CreateResponse(pipeline);
    }
}
