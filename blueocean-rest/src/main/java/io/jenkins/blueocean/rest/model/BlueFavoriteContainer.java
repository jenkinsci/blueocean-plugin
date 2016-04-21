package io.jenkins.blueocean.rest.model;

import io.jenkins.blueocean.commons.stapler.JsonBody;
import org.kohsuke.stapler.WebMethod;
import org.kohsuke.stapler.verb.POST;

/**
 * @author Ivan Meredith
 */
public abstract class BlueFavoriteContainer extends Container<BlueFavorite> {

    public abstract BlueFavorite get(String name);

}
