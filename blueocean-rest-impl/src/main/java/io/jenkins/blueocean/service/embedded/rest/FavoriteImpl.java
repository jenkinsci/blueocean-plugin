package io.jenkins.blueocean.service.embedded.rest;

import io.jenkins.blueocean.rest.hal.Link;
import io.jenkins.blueocean.rest.model.BlueFavorite;
import io.jenkins.blueocean.rest.model.BlueFavoriteContainer;
import org.kohsuke.stapler.WebMethod;
import org.kohsuke.stapler.verb.GET;

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

    @Override
    public Link getLink() {
        return user.getLink().rel("favorites");
    }
}
