package io.jenkins.blueocean.rest;

import io.jenkins.blueocean.rest.hal.Link;

/**
 * Reachable HTTP resource
 *
 * @author Vivek Pandey
 */
public interface Reachable {

    /**
     * @return Gives {@link Link} to itself
     */
    Link getLink();
}
