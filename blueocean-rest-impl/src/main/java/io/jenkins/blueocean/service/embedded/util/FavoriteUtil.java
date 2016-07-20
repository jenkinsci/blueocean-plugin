package io.jenkins.blueocean.service.embedded.util;

import hudson.Util;
import hudson.model.Item;
import hudson.model.ItemGroup;
import hudson.model.Job;
import hudson.model.User;
import hudson.plugins.favorite.FavoritePlugin;
import hudson.plugins.favorite.user.FavoriteUserProperty;
import io.jenkins.blueocean.commons.ServiceException;
import io.jenkins.blueocean.rest.Reachable;
import io.jenkins.blueocean.rest.hal.Link;
import io.jenkins.blueocean.rest.hal.LinkResolver;
import io.jenkins.blueocean.rest.model.BlueFavorite;
import io.jenkins.blueocean.rest.model.BluePipeline;
import io.jenkins.blueocean.service.embedded.rest.BlueFavoriteResolver;
import io.jenkins.blueocean.service.embedded.rest.BluePipelineFactory;
import io.jenkins.blueocean.service.embedded.rest.FavoriteImpl;
import io.jenkins.blueocean.service.embedded.rest.UserImpl;
import jenkins.model.Jenkins;
import org.kohsuke.stapler.Stapler;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

/**
 * @author Ivan Meredith
 */
public class FavoriteUtil {
    public static void favoriteJob(String fullName, boolean favorite) {
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
    }

    public static Link getFavoriteLink(String fullName){
        User user = User.current();
        if(user != null) {
            return new UserImpl(user).getLink().rel("favorites/"+ FavoriteUtil.encodeFullName(fullName));
        }
        return null;
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


    public static BlueFavorite getFavorite(String fullName, Reachable parent){
        Item item = Jenkins.getInstance().getItem(fullName);
        return getFavorite(item,parent);
    }

    public static BlueFavorite getFavorite(Item item){
        LinkResolver linkResolver = Jenkins.getInstance().getInjector().getInstance(LinkResolver.class);

        final Link l = linkResolver.resolve(item);
        if(l !=null) {
            return getFavorite(item, new Reachable() {
                @Override
                public Link getLink() {
                    return l.ancestor();
                }
            });
        }
        return null;
    }
    /**
     *  Gets favorite model for given model model
     *
     *  First it tries to find favorite model using {@link BlueFavoriteResolver}, if none found then it simply gets the
     *  mapped blueocean API resource for the given favorite item, creates BlueFavorite and returns.
     *
     * @param item favorited model object
     * @param parent {@link Reachable} parent of BlueOcean favorited API resource. It might be null, in that case parent
     *               is computed using {@link LinkResolver#resolve(Object)}
     * @return resolved favorite object if found otherwise null
     */
    public static BlueFavorite getFavorite(Item item, @Nonnull Reachable parent){
        if(item == null){
            return null;
        }

        //If there is a resolver to resolve this favorite item to another model object as favorite
        for(BlueFavoriteResolver resolver: BlueFavoriteResolver.all()){
            BlueFavorite blueFavorite = resolver.resolve(item,parent);
            if(blueFavorite != null){
                return blueFavorite;
            }
        }

        //otherwise, default
        Link favouriteLink = getFavoriteLink(item.getFullName());
        if(favouriteLink == null){
            return null;
        }

        BluePipeline pipeline = BluePipelineFactory.getPipelineInstance(item, parent);
        if(pipeline != null){
            return new FavoriteImpl(pipeline,favouriteLink);
        }

        return null;
    }

}
