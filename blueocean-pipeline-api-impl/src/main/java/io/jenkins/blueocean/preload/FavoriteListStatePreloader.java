package io.jenkins.blueocean.preload;

import hudson.Extension;
import hudson.model.User;
import hudson.plugins.favorite.user.FavoriteUserProperty;
import io.jenkins.blueocean.commons.PageStatePreloader;
import net.sf.json.JSONArray;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 * Loads the list of item full names for favorites
 */
@Extension
public class FavoriteListStatePreloader extends PageStatePreloader {
    @Nonnull
    @Override
    public String getStatePropertyPath() {
        return "favoritesList";
    }

    @CheckForNull
    @Override
    public String getStateJson() {
        User jenkinsUser = User.current();
        FavoriteUserProperty fup = jenkinsUser.getProperty(FavoriteUserProperty.class);
        return JSONArray.fromObject(fup.getAllFavorites()).toString();
    }
}
