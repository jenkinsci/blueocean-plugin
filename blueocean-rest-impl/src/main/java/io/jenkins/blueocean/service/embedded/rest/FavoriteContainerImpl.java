package io.jenkins.blueocean.service.embedded.rest;

import com.cloudbees.hudson.plugins.folder.AbstractFolder;
import hudson.model.Item;
import hudson.plugins.favorite.Favorites;
import io.jenkins.blueocean.rest.Reachable;
import io.jenkins.blueocean.rest.hal.Link;
import io.jenkins.blueocean.rest.model.BlueFavorite;
import io.jenkins.blueocean.rest.model.BlueFavoriteContainer;
import io.jenkins.blueocean.service.embedded.util.FavoriteUtil;
import jenkins.model.Jenkins;
import org.kohsuke.stapler.StaplerResponse;
import org.kohsuke.stapler.WebMethod;
import org.kohsuke.stapler.verb.DELETE;

import javax.servlet.http.HttpServletResponse;
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
        Item item = Jenkins.getInstance().getItemByFullName(name);
        if(item != null && Favorites.isFavorite(user.user, item)){
            return FavoriteUtil.getFavorite(item, this);
        }
        return null;
    }

    @Override
    public Iterator<BlueFavorite> iterator() {
        List<BlueFavorite> favorites = new ArrayList<>();

        for(final Item favorite: Favorites.getFavorites(user.user)){
            if(favorite instanceof AbstractFolder) {
                continue;
            }
            BlueFavorite blueFavorite = FavoriteUtil.getFavorite(favorite);
            if(blueFavorite != null){
                favorites.add(blueFavorite);
            }
        }
        return favorites.iterator();
    }



    @Override
    public Link getLink() {
        return self;
    }

    /**
     * Delete all of the user's favorites.
     */
    @WebMethod(name = "") @DELETE
    public void doDelete(StaplerResponse resp) throws Favorites.FavoriteException {
        for (final Item favorite: Favorites.getFavorites(user.user)) {
            Favorites.removeFavorite(user.user, favorite);
        }
        resp.setStatus(HttpServletResponse.SC_NO_CONTENT);
    }
}
