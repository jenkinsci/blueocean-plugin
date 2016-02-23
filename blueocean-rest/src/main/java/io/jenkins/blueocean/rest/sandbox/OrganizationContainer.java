package io.jenkins.blueocean.rest.sandbox;

import hudson.ExtensionPoint;
import io.jenkins.blueocean.api.profile.CreateOrganizationRequest;
import io.jenkins.blueocean.commons.stapler.JsonBody;
import org.kohsuke.stapler.WebMethod;

/**
 * This is the head of the blue ocean API.
 *
 * @author Kohsuke Kawaguchi
 */
public abstract class OrganizationContainer extends Container<Organization> implements ExtensionPoint {
    @WebMethod(name="",method="POST")
    public abstract Organization create(@JsonBody CreateOrganizationRequest req);
}
