package io.jenkins.blueocean.listeners;

import hudson.model.Action;

/**
 * Marker interface for actions we want to coalesce into BluePipelineNodes from the various pipeline FlowNodes which they
 * represent.
 *
 * Allows us to get the actions we're interested in promoting via Item.getActions(Class), without having to get all
 * actions which could be an arbitrarily large amount of wasted work.
 */
public interface BluePipelineAction extends Action {
}
