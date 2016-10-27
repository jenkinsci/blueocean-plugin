package io.jenkins.blueocean.rest.model;

import io.jenkins.blueocean.commons.ServiceException;
import io.jenkins.blueocean.commons.stapler.JsonBody;
import org.kohsuke.stapler.WebMethod;
import org.kohsuke.stapler.verb.POST;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * BluePipeline container
 *
 * @author Kohsuke Kawaguchi
 */
public abstract class BluePipelineContainer extends Container<BluePipeline>{

    /**
     * Create new pipeline.
     *
     * @param request {@link BluePipelineCreateRequest} request object
     * @return {@link CreateResponse} response
     */
    @POST
    @WebMethod(name = "")
    public  CreateResponse create(@JsonBody BluePipelineCreateRequest request) throws IOException{
        Map<String,String> errors = new HashMap<>();
        if(request.getName() == null || request.getName().trim().isEmpty()){
            errors.put("name", "creatorId is required parameter");

        }
        if(request.getCreatorId() == null || request.getCreatorId().trim().isEmpty()){
            errors.put("creatorId", "creatorId is required parameter");
        }
        if(!errors.isEmpty()) {
            ServiceException.ErrorMessage message = new ServiceException.ErrorMessage(400, "Bad Request");
            throw new ServiceException.BadRequestExpception(message.add(errors));
        }
        for(BluePipelineCreator f: BluePipelineCreator.all()){
            if(f.getId().equals(request.getCreatorId().trim())){

                return new CreateResponse(f.create(request, this));
            }
        }
        throw new ServiceException.BadRequestExpception("Invalid creatorId: "+request.getCreatorId());
    }
}
