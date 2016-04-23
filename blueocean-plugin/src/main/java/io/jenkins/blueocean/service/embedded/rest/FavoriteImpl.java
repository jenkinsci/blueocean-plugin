package io.jenkins.blueocean.service.embedded.rest;

import hudson.plugins.favorite.FavoritePlugin;
import io.jenkins.blueocean.commons.ServiceException;
import io.jenkins.blueocean.rest.model.BlueFavorite;
import io.jenkins.blueocean.rest.model.BlueFavoriteContainer;
import jenkins.model.Jenkins;
import org.kohsuke.stapler.Stapler;
import org.kohsuke.stapler.WebMethod;
import org.kohsuke.stapler.verb.GET;
import org.kohsuke.stapler.verb.POST;

import java.io.IOException;
import java.util.Collections;
import java.util.Iterator;

/**
 * @author Ivan Meredith
 */
public class FavoriteImpl extends BlueFavoriteContainer {
    private final UserImpl user;
    public FavoriteImpl(UserImpl user) {
        this.user = user;
    }

    @Override
    public BlueFavorite get(final String name) {
        return user.getFavorite(name);
    }

    @Override
    @GET
    @WebMethod(name="")
    public Iterator<BlueFavorite> iterator() {
        return user.getFavouriteIterator();
    }

}
