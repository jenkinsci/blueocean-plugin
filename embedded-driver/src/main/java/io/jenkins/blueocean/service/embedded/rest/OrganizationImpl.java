package io.jenkins.blueocean.service.embedded.rest;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.jenkins.blueocean.commons.ServiceException;
import io.jenkins.blueocean.commons.stapler.JsonBody;
import io.jenkins.blueocean.rest.sandbox.Organization;
import io.jenkins.blueocean.rest.sandbox.PipelineContainer;
import jenkins.model.Jenkins;
import org.kohsuke.stapler.WebMethod;
import org.kohsuke.stapler.verb.DELETE;
import org.kohsuke.stapler.verb.POST;

import java.io.IOException;

/**
 * {@link Organization} implementation for the embedded use.
 *
 * @author Vivek Pandey
 * @author Kohsuke Kawaguchi
 */
public class OrganizationImpl extends Organization {
    /**
     * In embedded mode, there's only one organization
     */
    public OrganizationImpl() {
    }

    /**
     * In embedded mode, there's only one organization
     */
    @JsonProperty
    public String getName() {
        return Jenkins.getInstance().getDisplayName().toLowerCase();
    }

    @Override
    public PipelineContainer getPipelines() {
        return null;
    }

    @WebMethod(name="") @DELETE
    public void delete() {
        throw new ServiceException.NotImplementedException("Not implemented yet");
    }

    @WebMethod(name="") @POST
    public void update(@JsonBody OrganizationImpl given) throws IOException {
        given.validate();
        throw new ServiceException.NotImplementedException("Not implemented yet");
//        getXmlFile().write(given);
    }

    private void validate() {
//        if (name.length()<2)
//            throw new IllegalArgumentException("Invalid name: "+name);
    }
}
