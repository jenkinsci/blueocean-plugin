package io.jenkins.blueocean.rest.sandbox.embedded;

import io.jenkins.blueocean.rest.sandbox.Organization;
import io.jenkins.blueocean.rest.sandbox.OrganizationContainer;

import java.util.Iterator;

/**
 * @author Kohsuke Kawaguchi
 */
public class OrganizationContainerImpl extends OrganizationContainer {
    @Override
    public Organization create(String name) {
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
