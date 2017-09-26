package io.jenkins.blueocean.rest;

import hudson.ExtensionPoint;
import io.jenkins.blueocean.Routable;
import io.jenkins.blueocean.rest.model.BlueUser;

/**
 * Route contributing to {@link io.jenkins.blueocean.rest.model.BlueUser}: url path /organization/:id/users/:user/:userRoute.urlName()
 */
public interface UserRoute extends Routable, ExtensionPoint {
    /**
     * Returns context based on the provided user
     * @param user the user
     * @return further rest traversal
     */
    Object get(BlueUser user);
}
