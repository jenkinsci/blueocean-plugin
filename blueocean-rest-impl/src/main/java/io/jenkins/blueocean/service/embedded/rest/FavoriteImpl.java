package io.jenkins.blueocean.service.embedded.rest;

import io.jenkins.blueocean.rest.hal.Link;
import io.jenkins.blueocean.rest.model.BlueFavorite;

/**
 * @author Vivek Pandey
 */
public class FavoriteImpl extends BlueFavorite {

    private final Object item;
    private final Link self;

    public FavoriteImpl(Object item, Link self) {
        this.self = self;
        this.item = item;
    }

    @Override
    public Object getItem() {
        return item;
    }

    @Override
    public Link getLink() {
        return self;
    }

}
