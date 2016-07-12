package io.jenkins.blueocean.service.embedded.rest;

import hudson.model.Item;
import io.jenkins.blueocean.rest.Reachable;
import io.jenkins.blueocean.rest.hal.Link;
import io.jenkins.blueocean.rest.hal.LinkResolver;
import io.jenkins.blueocean.rest.model.BlueFavorite;
import jenkins.model.Jenkins;
import io.jenkins.blueocean.service.embedded.util.FavoriteUtil;

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

    public FavoriteImpl(Item item, Reachable parent) {
        this.self = parent.getLink().rel(FavoriteUtil.encodeFullName(item.getFullName()));
        LinkResolver linkResolver = Jenkins.getInstance().getInjector().getInstance(LinkResolver.class);

        final Link link = linkResolver.resolve(item);

        this.item = BluePipelineFactory.getPipelineInstance(item, new Reachable() {
                @Override
                public Link getLink() {
                    return link.ancestor();
                }
            });

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
