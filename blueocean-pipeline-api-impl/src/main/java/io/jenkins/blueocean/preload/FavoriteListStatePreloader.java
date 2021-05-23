package io.jenkins.blueocean.preload;

import hudson.Extension;
import hudson.model.Item;
import hudson.model.Job;
import hudson.model.User;
import hudson.plugins.favorite.user.FavoriteUserProperty;
import io.jenkins.blueocean.commons.PageStatePreloader;
import jenkins.model.Jenkins;
import jenkins.scm.api.metadata.PrimaryInstanceMetadataAction;
import net.sf.json.JSONArray;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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
        if (jenkinsUser == null) {
            return null;
        }
        FavoriteUserProperty fup = jenkinsUser.getProperty(FavoriteUserProperty.class);
        if (fup == null) {
            return null;
        }
        Set<String> favorites = fup.getAllFavorites();
        if (favorites == null) {
            return null;
        }

        final Jenkins jenkins = Jenkins.get();
        final List<FavoritPreload> favoritPreloads = favorites.stream()
            .map(name -> {
                final Item item = jenkins.getItemByFullName(name);
                if (item instanceof Job) {
                    final Job<?, ?> job = (Job<?, ?>) item;
                    if (job.getAction(PrimaryInstanceMetadataAction.class) != null) {
                        return new FavoritPreload(name, true);
                    }
                }
                return new FavoritPreload(name, false);
            }).collect(Collectors.toList());

        return JSONArray.fromObject(favoritPreloads).toString();
    }

    public static class FavoritPreload {
        private final String name;
        private final boolean isPrimary;

        public FavoritPreload(String name, boolean isPrimary) {
            this.name = name;
            this.isPrimary = isPrimary;
        }

        public String getName() {
            return name;
        }

        public boolean isPrimary() {
            return isPrimary;
        }
    }
}
