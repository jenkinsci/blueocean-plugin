package io.jenkins.blueocean.rest.factory;

import hudson.model.Action;

import javax.annotation.Nonnull;

/**
 * {@link Action} to provide BlueOcean Url for a Jenkins model in context.
 *
 * @author Vivek Pandey
 * @see BlueOceanUrlFactory
 */
public interface BlueOceanUrlAction extends Action {
    /**
     * Gives BlueOcean URL for Jenkins model object in context.
     *
     * If there is no mapping it's up to implementation to provide a default landing page.
     * @return URL corresponding to the Jenkins model
     */
    @Nonnull String getUrl();
}
