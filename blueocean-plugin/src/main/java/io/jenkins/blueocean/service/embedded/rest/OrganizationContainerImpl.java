package io.jenkins.blueocean.service.embedded.rest;

import hudson.Extension;
import io.jenkins.blueocean.api.profile.CreateOrganizationRequest;
import io.jenkins.blueocean.commons.ServiceException;
import io.jenkins.blueocean.commons.stapler.JsonBody;
import io.jenkins.blueocean.rest.sandbox.BOOrganization;
import io.jenkins.blueocean.rest.sandbox.BOOrganizationContainer;
import io.jenkins.blueocean.rest.sandbox.BOUserContainer;
import jenkins.model.Jenkins;
import org.kohsuke.stapler.WebMethod;
import org.kohsuke.stapler.verb.POST;

import javax.inject.Inject;
import java.util.Collections;
import java.util.Iterator;

/**
 * {@link BOOrganizationContainer} for the embedded use
 *
 * @author Vivek Pandey
 * @author Kohsuke Kawaguchi
 */
@Extension
public class OrganizationContainerImpl extends BOOrganizationContainer {
    @Inject
    UserContainerImpl users;

    @Override
    public BOOrganization get(String name) {
        validateOrganization(name);
        return OrganizationImpl.INSTANCE;
    }

    @Override
    // TODO: these three annotations are redundant as the same things are defined in the base method
    // TODO: improve Stapler to remove them
    @WebMethod(name="") @POST
    public BOOrganization create(@JsonBody CreateOrganizationRequest req) {
        throw new ServiceException.NotImplementedException("Not implemented yet");
    }

    @Override
    public Iterator<BOOrganization> iterator() {
        return Collections.<BOOrganization>singleton(OrganizationImpl.INSTANCE).iterator();
    }

    protected void validateOrganization(String organization){
        if (!organization.equals(Jenkins.getActiveInstance().getDisplayName().toLowerCase())) {
            throw new ServiceException.UnprocessableEntityException(String.format("Organization %s not found",
                organization));
        }
    }

    /**
     * In the embedded case, there's only one organization and everyone belongs there,
     * so we can just return that singleton.
     */
    @Override
    public BOUserContainer getUsers() {
        return users;
    }
}
