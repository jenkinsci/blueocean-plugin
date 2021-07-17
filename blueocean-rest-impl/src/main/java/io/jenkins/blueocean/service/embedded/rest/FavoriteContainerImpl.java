package io.jenkins.blueocean.service.embedded.rest;

import com.cloudbees.hudson.plugins.folder.AbstractFolder;
import hudson.model.Item;
import hudson.plugins.favorite.Favorites;
import io.jenkins.blueocean.rest.Reachable;
import io.jenkins.blueocean.rest.Utils;
import io.jenkins.blueocean.rest.hal.Link;
import io.jenkins.blueocean.rest.model.BlueFavorite;
import io.jenkins.blueocean.rest.model.BlueFavoriteContainer;
import io.jenkins.blueocean.rest.pageable.PagedResponse;
import io.jenkins.blueocean.service.embedded.util.FavoriteUtil;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.servlet.http.HttpServletResponse;
import jenkins.model.Jenkins;
import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.Stapler;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;
import org.kohsuke.stapler.WebMethod;
import org.kohsuke.stapler.verb.DELETE;

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
        Item item = Jenkins.get().getItemByFullName(name);
        if(item != null && Favorites.isFavorite(user.user, item)){
            return FavoriteUtil.getFavorite(item, this);
        }
        return null;
    }

    @Override
    public Iterator<BlueFavorite> iterator() {
        StaplerRequest request = Stapler.getCurrentRequest();
        int start=0;
        int limit = PagedResponse.DEFAULT_LIMIT;

        if(request != null) {
            String startParam = request.getParameter("start");
            if (StringUtils.isNotBlank(startParam)) {
                start = Integer.parseInt(startParam);
            }

            String limitParam = request.getParameter("limit");
            if (StringUtils.isNotBlank(limitParam)) {
                limit = Integer.parseInt(limitParam);
            }
        }

        return iterator(start, limit);
    }

    @Override
    public Iterator<BlueFavorite> iterator(int start, int limit) {
        List<BlueFavorite> favorites = new ArrayList<>();

        Iterator<Item> favoritesIterator = Favorites.getFavorites(user.user).iterator();
        Utils.skip(favoritesIterator, start);
        int count = 0;
        while(count < limit && favoritesIterator.hasNext()) {
            Item item = favoritesIterator.next();
            if(item instanceof AbstractFolder) {
                continue;
            }
            BlueFavorite blueFavorite = FavoriteUtil.getFavorite(item);
            if(blueFavorite != null){
                favorites.add(blueFavorite);
                count++;
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
