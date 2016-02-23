package io.jenkins.blueocean.service.embedded.rest;

import hudson.Extension;
import io.jenkins.blueocean.api.profile.CreateOrganizationRequest;
import io.jenkins.blueocean.commons.stapler.JsonBody;
import io.jenkins.blueocean.rest.sandbox.Organization;
import io.jenkins.blueocean.rest.sandbox.OrganizationContainer;

import java.util.Iterator;

/**
 * @author Kohsuke Kawaguchi
 */
@Extension
public class OrganizationContainerImpl extends OrganizationContainer {
    @Override
    public Organization create(@JsonBody CreateOrganizationRequest req) {
        return null;
    }

    @Override
    public Organization get(String name) {
        return null;
    }

    @Override
    public Iterator<Organization> iterator() {
        return null;
    }
}
