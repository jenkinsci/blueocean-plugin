package io.jenkins.blueocean.service.embedded.rest;

import hudson.Extension;
import io.jenkins.blueocean.api.profile.CreateOrganizationRequest;
import io.jenkins.blueocean.commons.ServiceException;
import io.jenkins.blueocean.commons.stapler.JsonBody;
import io.jenkins.blueocean.rest.sandbox.Organization;
import io.jenkins.blueocean.rest.sandbox.OrganizationContainer;
import jenkins.model.Jenkins;
import org.kohsuke.stapler.WebMethod;
import org.kohsuke.stapler.verb.POST;

import java.util.Iterator;

/**
 * {@link OrganizationContainer} for the embedded use
 *
 * @author Vivek Pandey
 * @author Kohsuke Kawaguchi
 */
@Extension
public class OrganizationContainerImpl extends OrganizationContainer {
    @Override
    public Organization get(String name) {
        validateOrganization(name);
        return new OrganizationImpl();
    }

    @Override
    @WebMethod(name="") @POST
    public Organization create(@JsonBody CreateOrganizationRequest req) {
        throw new ServiceException.NotImplementedException("Not implemented yet");
    }

    @Override
    public Iterator<Organization> iterator() {
        return null;
    }

    protected void validateOrganization(String organization){
        if (!organization.equals(Jenkins.getActiveInstance().getDisplayName().toLowerCase())) {
            throw new ServiceException.UnprocessableEntityException(String.format("Organization %s not found",
                organization));
        }
    }
}
