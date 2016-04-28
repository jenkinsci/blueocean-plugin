package io.jenkins.blueocean.service.embedded.rest;

import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.jenkinsci.plugins.workflow.multibranch.WorkflowMultiBranchProject;

import hudson.model.Item;
import hudson.model.ItemGroup;
import hudson.model.User;
import hudson.plugins.favorite.user.FavoriteUserProperty;
import hudson.tasks.Mailer;
import io.jenkins.blueocean.commons.ServiceException;
import io.jenkins.blueocean.rest.model.BlueFavorite;
import io.jenkins.blueocean.rest.model.BlueFavoriteContainer;
import io.jenkins.blueocean.rest.model.BlueUser;
import io.jenkins.blueocean.service.embedded.util.FavoriteUtil;
import jenkins.model.Jenkins;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * {@link BlueUser} implementation backed by in-memory {@link User}
 *
 * @author Kohsuke Kawaguchi
 * @author Vivek Pandey
 */
public class UserImpl extends BlueUser {
    private final User user;

    public UserImpl(User user) {
        this.user = user;
    }

    @Override
    public String getId() {
        return user.getId();
    }

    @Override
    public String getFullName() {
        return user.getFullName();
    }

    @Override
    public String getEmail() {
        if (!user.hasPermission(Jenkins.ADMINISTER)) return null;

        Mailer.UserProperty p = user.getProperty(Mailer.UserProperty.class);
        return p != null ? p.getAddress() : null;
    }

    public BlueFavorite getFavorite(final String name){
        FavoriteUserProperty prop = user.getProperty(FavoriteUserProperty.class);
        if(prop == null) {
            return null;
        }

        if(prop.isJobFavorite(name)) {

            return new BlueFavorite(name);
        }

        return null;
    }

    public Iterator<BlueFavorite> getFavouriteIterator(){
        FavoriteUserProperty prop = user.getProperty(FavoriteUserProperty.class);
        List<BlueFavorite> pipelines = new ArrayList<BlueFavorite>();
        Jenkins j = Jenkins.getInstance();

        String org = j.getDisplayName().toLowerCase();
        for(final String favorite: prop.getFavorites()){
            Item i = j.getItem(favorite,j);
            if(i == null) {
                continue;
            }
            pipelines.add(new BlueFavorite(FavoriteUtil.generateBlueUrl(org,i)));
        }
        return pipelines.iterator();
    }


    @Override
    public BlueFavoriteContainer getFavorites() {
        String name = Jenkins.getAuthentication().getName();
        if(!user.getId().equals(name)) {
            throw new ServiceException.ForbiddenException("You do not have access to this resource.");
        }
        return new FavoriteImpl(this);
    }
}
