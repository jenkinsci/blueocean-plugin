package io.jenkins.blueocean.rest.sandbox;

import hudson.ExtensionPoint;
import org.kohsuke.stapler.WebMethod;

import java.util.Iterator;

/**
 * @author Kohsuke Kawaguchi
 */
public abstract class OrganizationContainer extends Container<Organization> implements ExtensionPoint {
    @WebMethod(name="",method="POST")
    public abstract Organization create(@JsonParam String name);
}
