package io.jenkins.blueocean.service.embedded.rest;

import hudson.Extension;
import io.jenkins.blueocean.commons.ServiceException;
import io.jenkins.blueocean.rest.model.BlueOrganization;
import io.jenkins.blueocean.rest.model.BlueOrganizationContainer;
import io.jenkins.blueocean.rest.model.BlueUserContainer;
import jenkins.model.Jenkins;

import javax.inject.Inject;
import java.util.Collections;
import java.util.Iterator;

/**
 * {@link BlueOrganizationContainer} for the embedded use
 *
 * @author Vivek Pandey
 * @author Kohsuke Kawaguchi
 */
@Extension
public class OrganizationContainerImpl extends BlueOrganizationContainer {
    @Override
    public BlueOrganization get(String name) {
        validateOrganization(name);
        return OrganizationImpl.INSTANCE;
    }

    @Override
    public Iterator<BlueOrganization> iterator() {
        return Collections.<BlueOrganization>singleton(OrganizationImpl.INSTANCE).iterator();
    }

    protected void validateOrganization(String organization){
        if (!organization.equals(Jenkins.getActiveInstance().getDisplayName().toLowerCase())) {
            throw new ServiceException.UnprocessableEntityException(String.format("Organization %s not found",
                organization));
        }
    }
}
