package io.jenkins.blueocean.rest.model;

/**
 * @author Ivan Meredith
 */
public abstract class BlueFavoriteContainer extends Container<BlueFavorite> {

    public abstract BlueFavorite get(String name);

}
