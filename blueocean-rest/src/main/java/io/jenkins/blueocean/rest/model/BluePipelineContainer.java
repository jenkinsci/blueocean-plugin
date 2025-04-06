package io.jenkins.blueocean.rest.model;

import io.jenkins.blueocean.commons.ErrorMessage;
import io.jenkins.blueocean.commons.ServiceException;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.StaplerRequest2;
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

    protected final BlueOrganization organization;

    public BluePipelineContainer(BlueOrganization organization) {
        this.organization = organization;
    }

    /**
     * Create new pipeline.
     *
     * @param body {@link BluePipelineCreateRequest} request object
     * @return {@link CreateResponse} response
     */
    @POST
    @WebMethod(name = "")
    public  CreateResponse create(@JsonBody JSONObject body, StaplerRequest2 staplerRequest) throws IOException{
        ErrorMessage err = new ErrorMessage(400, "Failed to create Git pipeline");

        if(body.get("name") == null){
            err.add(new ErrorMessage.Error("name", ErrorMessage.Error.ErrorCodes.MISSING.toString(), "name is required"));
        }

        if(body.get("$class") == null){
            err.add(new ErrorMessage.Error("$class", ErrorMessage.Error.ErrorCodes.MISSING.toString(), "$class is required"));
        }

        if(!err.getErrors().isEmpty()){
            throw new ServiceException.BadRequestException(err);
        }

        BluePipelineCreateRequest request = staplerRequest.bindJSON(BluePipelineCreateRequest.class, body);
        return create(request);
    }

    public CreateResponse create(BluePipelineCreateRequest request) throws IOException {
        BluePipeline pipeline = request.create(organization, this);
        if(pipeline == null){
            throw new ServiceException.UnexpectedErrorException("Failed to create pipeline: "+request.getName());
        }
        return new CreateResponse(pipeline);
    }
}
