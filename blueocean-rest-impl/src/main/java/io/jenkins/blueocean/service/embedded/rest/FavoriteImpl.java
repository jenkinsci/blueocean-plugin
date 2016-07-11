package io.jenkins.blueocean.service.embedded.rest;

import hudson.Util;
import hudson.model.Item;
import io.jenkins.blueocean.rest.Reachable;
import io.jenkins.blueocean.rest.hal.Link;
import io.jenkins.blueocean.rest.hal.LinkResolver;
import io.jenkins.blueocean.rest.model.BlueFavorite;
import io.jenkins.blueocean.rest.model.BluePipeline;
import jenkins.model.Jenkins;

/**
 * @author Vivek Pandey
 */
public class FavoriteImpl extends BlueFavorite {

    private final Object item;
    private final Link self;
    private final LinkResolver linkResolver;

    public FavoriteImpl(Item item, Reachable parent) {
        this.self = parent.getLink().rel(Util.rawEncode(item.getFullName()));
        this.linkResolver = Jenkins.getActiveInstance().getInjector().getInstance(LinkResolver.class);

        final Link link = linkResolver.resolve(item);

        BluePipeline pipeline = null;
        for(BluePipelineFactory factory:BluePipelineFactory.all()){

            pipeline = factory.getPipeline(item, new Reachable() {
                @Override
                public Link getLink() {
                    return link.ancestor();
                }
            });

            if(pipeline != null){
                break;
            }
        }

        this.item = pipeline;
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
