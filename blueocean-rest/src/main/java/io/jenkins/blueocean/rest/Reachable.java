package io.jenkins.blueocean.rest;

import io.jenkins.blueocean.rest.hal.Link;

/**
 * Reachable HTTP resource
 *
 * @author Vivek Pandey
 */
public interface Reachable {

    /**
     * @return Gives {@link Link} to a reachable Resource or Container
     */
    Link getLink();
}
