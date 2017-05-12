package io.jenkins.blueocean.service.embedded.rest;

import hudson.Extension;
import io.jenkins.blueocean.commons.ServiceException;
import io.jenkins.blueocean.rest.ApiHead;
import io.jenkins.blueocean.rest.factory.organization.OrganizationFactory;
import io.jenkins.blueocean.rest.hal.Link;
import io.jenkins.blueocean.rest.model.BlueOrganization;
import io.jenkins.blueocean.rest.model.BlueOrganizationContainer;

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
        BlueOrganization o = OrganizationFactory.getInstance().get(name);
        if (o==null)
            throw new ServiceException.NotFoundException(String.format("Organization %s not found",name));
        return o;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Iterator<BlueOrganization> iterator() {
        return OrganizationFactory.getInstance().list().iterator();
    }

    @Override
    public Link getLink() {
        return ApiHead.INSTANCE().getLink().rel("organizations");
    }
}
