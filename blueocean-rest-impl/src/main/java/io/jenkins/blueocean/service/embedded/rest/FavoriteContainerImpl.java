package io.jenkins.blueocean.service.embedded.rest;

import hudson.model.Item;
import hudson.plugins.favorite.user.FavoriteUserProperty;
import io.jenkins.blueocean.rest.Reachable;
import io.jenkins.blueocean.rest.hal.Link;
import io.jenkins.blueocean.rest.model.BlueFavorite;
import io.jenkins.blueocean.rest.model.BlueFavoriteContainer;
import io.jenkins.blueocean.service.embedded.util.FavoriteUtil;
import jenkins.model.Jenkins;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @author Ivan Meredith
 * @author Vivek Pandey
 */
public class FavoriteContainerImpl extends BlueFavoriteContainer {
    private final UserImpl user;
    private final Link self;
    public FavoriteContainerImpl(UserImpl user, Reachable parent) {
        this.user = user;
        this.self = parent.getLink().rel("favorites");
    }

    @Override
    public BlueFavorite get(String name) {
        name = FavoriteUtil.decodeFullName(name);
        if(user.isFavorite(name)){
            Item item = Jenkins.getInstance().getItemByFullName(name);
            if(FavoriteUtil.isFavorableItem(item)) {
                return new FavoriteImpl(item, this);
            }
        }
        return null;
    }

    @Override
    public Iterator<BlueFavorite> iterator() {
        FavoriteUserProperty prop = user.getFavoriteProperty();
        List<BlueFavorite> pipelines = new ArrayList<>();
        Jenkins j = Jenkins.getInstance();

        for(final String favorite: prop.getFavorites()){
            Item i = j.getItemByFullName(favorite, Item.class);
            if(FavoriteUtil.isFavorableItem(i)){
                pipelines.add(new FavoriteImpl(i, this));
            }
        }
        return pipelines.iterator();
    }

    @Override
    public Link getLink() {
        return self;
    }
}
