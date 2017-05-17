package io.jenkins.blueocean.rest.factory;

import hudson.model.Action;
import hudson.model.RootAction;

import javax.annotation.Nonnull;

/**
 * {@link Action} to provide BlueOcean Url for a Jenkins model in context.
 *
 * @author Vivek Pandey
 * @see BlueOceanUrlActionFactory
 */
public interface BlueOceanUrlAction extends RootAction {
    /**
     * Gives BlueOcean URL for underlying Jenkins model object in context.
     *
     * If there is no mapping it's up to implementation to provide a default landing page.
     * @return URL corresponding to the Jenkins model
     */
    @Nonnull String getUrl();
}
