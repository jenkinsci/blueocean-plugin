package io.jenkins.blueocean.rest.factory;

import hudson.ExtensionList;
import hudson.ExtensionPoint;
import hudson.model.Item;
import io.jenkins.blueocean.rest.Reachable;
import io.jenkins.blueocean.rest.model.BlueFavorite;

/**
 * Resolves favorite for a given model object {@link Item}
 *
 * For example:
 *
 * A favorite multi-branch project might resolve to a master branch pipeline
 *
 * @author Vivek Pandey
 */
public abstract class BlueFavoriteResolver implements ExtensionPoint {

    /**
     * Resolves a favorite {@link Item} to another model object as {@link BlueFavorite}
     *
     * @param item given favorite item that might resolve to another model object as favorite
     *
     * @return favorite item. null if it can't resolve.
     */
    public abstract BlueFavorite resolve(Item item, Reachable parent);


    public static ExtensionList<BlueFavoriteResolver> all(){
        return ExtensionList.lookup(BlueFavoriteResolver.class);
    }
}
