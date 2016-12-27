package io.jenkins.blueocean.rest;

import hudson.ExtensionPoint;
import io.jenkins.blueocean.Routable;

/**
 * Route contributing to {@link io.jenkins.blueocean.rest.model.BlueOrganization}: url path /organization/:id/:organizationRoute.urlName()
 *
 * @author Vivek Pandey
 */
public interface OrganizationRoute extends Routable, ExtensionPoint {
}
