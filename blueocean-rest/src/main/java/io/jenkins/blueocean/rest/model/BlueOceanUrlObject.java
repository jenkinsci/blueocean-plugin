package io.jenkins.blueocean.rest.model;

import io.jenkins.blueocean.rest.factory.BlueOceanUrlObjectFactory;

import javax.annotation.Nonnull;

/**
 * Defines properties associated with mapping of class Jenkins ModeObject to a a menu Action item to jump
 * to respective BlueOcean page.
 *
 * @author Vivek Pandey
 * @see BlueOceanUrlObjectFactory
 */
public abstract class BlueOceanUrlObject{
    /**
     * @return Gives display name of the link to open BlueOcean page. e.g. 'Open Blue Ocean'
     */
    public @Nonnull abstract String getDisplayName();

    /**
     * @return Gives url to the BlueOcean page.
     */
    public @Nonnull abstract String getUrl();

    /**
     * @return Gives Icon URL that goes with the link
     */
    public @Nonnull abstract String getIconUrl();
}