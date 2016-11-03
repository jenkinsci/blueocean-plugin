package io.jenkins.blueocean.service.embedded.util;

import com.cloudbees.hudson.plugins.folder.AbstractFolder;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import hudson.model.Item;
import hudson.model.Job;
import hudson.model.TopLevelItem;
import hudson.model.User;
import hudson.plugins.favorite.FavoritePlugin;
import hudson.plugins.favorite.Favorites;
import hudson.plugins.favorite.Favorites.FavoriteException;
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

    private static final String DEFAULT_BRANCH = "master";

    public static void setFavorite(Job job) {
        try {
            Favorites.addFavorite(getUser(), job);
        } catch (FavoriteException e) {
            throw new ServiceException.UnexpectedErrorException("Something went wrong setting the favorite", e);
        }
    }

    public static void unsetFavorite(Job job) {
        try {
            Favorites.addFavorite(getUser(), job);
        } catch (FavoriteException e) {
            throw new ServiceException.UnexpectedErrorException("Something went wrong setting the favorite", e);
        }
    }

    public static String decodeFullName(String name){
        try {
            return URLDecoder.decode(URLDecoder.decode(name, "UTF-8"), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new ServiceException.UnexpectedErrorException("Something went wrong URL decoding fullName: "+name, e);
        }
    }

    /**
     * Checks if the user has a favorite entry for this job
     * e.g. this is true when the user has favoriteted or unfavoriteted a job
     * but not true for when a job has not been favorited by this user
     * @param job path
     * @return favorite
     */
    public static boolean hasFavourite(User user, Job job) {
        try {
            return Favorites.hasFavorite(user, job);
        } catch (FavoriteException e) {
            throw new ServiceException.UnexpectedErrorException(e.getMessage(), e);
        }
    }

    public static BlueFavorite getFavorite(String fullName, Reachable parent){
        Item item = Jenkins.getInstance().getItem(fullName);
        return getFavorite(item,parent);
    }

    public static BlueFavorite getFavorite(Item item){
        final Link l = LinkResolver.resolveLink(item);
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

        BluePipeline pipeline = BluePipelineFactory.getPipelineInstance(item, parent);
        if(pipeline != null){
            return new FavoriteImpl(pipeline,pipeline.getLink().rel("favorite"));
        }

        return null;
    }

    /**
     * Resolves the default branch for a folder
     * @param folder to check within
     * @return default branch
     */
    public static Job resolveDefaultBranch(AbstractFolder folder) {
        // TODO: lookup the multibranch project and look for a default branch property
        TopLevelItem job = folder.getJob(DEFAULT_BRANCH);
        if(job == null) {
            throw new ServiceException.BadRequestExpception("no master branch to favorite");
        }
        if (!(job instanceof Job)) {
            throw new ServiceException.MethodNotAllowedException(DEFAULT_BRANCH + " is not a job");
        }
        return (Job) job;
    }

    private static User getUser() {
        User user = User.current();
        if(user == null) {
            throw new ServiceException.ForbiddenException("Must be logged in to use set favorites");
        }
        return user;
    }
}
