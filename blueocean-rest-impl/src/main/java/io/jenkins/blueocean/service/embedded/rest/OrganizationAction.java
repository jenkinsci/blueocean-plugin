package io.jenkins.blueocean.service.embedded.rest;

import hudson.ExtensionPoint;
import hudson.model.Action;

/**
 * Actions contributing to {@link io.jenkins.blueocean.rest.model.BlueOrganization}: url path /organization/:id/:organizationAction.urlName()
 *
 * TODO: Copied from PR 593, remove after PR 593 is merged
 *
 * @author Vivek Pandey
 */
public interface OrganizationAction extends Action, ExtensionPoint {
}