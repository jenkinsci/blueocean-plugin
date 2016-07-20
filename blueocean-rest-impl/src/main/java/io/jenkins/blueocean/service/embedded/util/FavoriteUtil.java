package io.jenkins.blueocean.service.embedded.util;

import hudson.Util;
import hudson.model.Item;
import hudson.model.ItemGroup;
import hudson.model.Job;
import hudson.model.User;
import hudson.plugins.favorite.FavoritePlugin;
import hudson.plugins.favorite.user.FavoriteUserProperty;
import io.jenkins.blueocean.commons.ServiceException;
import io.jenkins.blueocean.rest.hal.Link;
import io.jenkins.blueocean.service.embedded.rest.UserImpl;
import jenkins.model.Jenkins;
import org.kohsuke.stapler.Stapler;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

/**
 * @author Ivan Meredith
 */
public class FavoriteUtil {
    public static Link favoriteJob(String fullName, boolean favorite) {
        User user = User.current();
        if(user == null) {
            throw new ServiceException.ForbiddenException("Must be logged in to use set favorites");
        }
        boolean set = false;
        FavoriteUserProperty fup = user.getProperty(FavoriteUserProperty.class);
        if(fup != null) {
            set = fup.isJobFavorite(fullName);
        }
        //TODO: FavoritePlugin is null
        FavoritePlugin plugin = Jenkins.getInstance().getPlugin(FavoritePlugin.class);
        if(plugin == null) {
            throw new ServiceException.UnexpectedErrorException("Can not find instance of Favorite Plugin");
        }
        if(favorite != set) {
            try {
                plugin.doToggleFavorite(Stapler.getCurrentRequest(), Stapler.getCurrentResponse(), fullName, Jenkins.getAuthentication().getName(), false);
            } catch (IOException e) {
                throw new ServiceException.UnexpectedErrorException("Something went wrong setting the favorite", e);
            }
        }
        return new UserImpl(user).getLink().rel("favorites/"+ Util.rawEncode(FavoriteUtil.encodeFullName(fullName)));
    }

    public static boolean isFavorableItem(Item i){
        return i!= null && (i instanceof Job || i instanceof ItemGroup);
    }

    public static String encodeFullName(String name){
        return Util.rawEncode(Util.rawEncode(name));
    }

    public static String decodeFullName(String name){
        try {
            return URLDecoder.decode(URLDecoder.decode(name, "UTF-8"), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new ServiceException.UnexpectedErrorException("Something went wrong URL decoding fullName: "+name, e);
        }
    }

}
