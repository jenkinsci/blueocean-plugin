package io.jenkins.blueocean.service.embedded.util;

import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.jenkinsci.plugins.workflow.multibranch.WorkflowMultiBranchProject;
import org.kohsuke.stapler.Stapler;

import java.io.IOException;

import hudson.model.Item;
import hudson.model.ItemGroup;
import hudson.model.Job;
import hudson.model.User;
import hudson.plugins.favorite.FavoritePlugin;
import hudson.plugins.favorite.user.FavoriteUserProperty;
import io.jenkins.blueocean.commons.ServiceException;
import jenkins.model.Jenkins;

/**
 * @author Ivan Meredith
 */
public class FavoriteUtil {
    public static void favoriteJob(Job job, boolean favorite) {
        User user = User.current();
        if(user == null) {
            throw new ServiceException.ForbiddenException("Must be logged in to use set favotites");
        }
        boolean set = false;
        FavoriteUserProperty fup = user.getProperty(FavoriteUserProperty.class);
        if(fup != null) {
            set = fup.isJobFavorite(job.getFullName());
        }
        //TODO: FavoritePlugin is null
        FavoritePlugin plugin = Jenkins.getInstance().getPlugin(FavoritePlugin.class);
        if(plugin == null) {
            throw new ServiceException.UnexpectedErrorException("Can not find instance of favorites plugin");
        }
        if(favorite != set) {
            try {
                plugin.doToggleFavorite(Stapler.getCurrentRequest(), Stapler.getCurrentResponse(), job.getFullName(), Jenkins.getAuthentication().getName(), false);
            } catch (IOException e) {
                throw new ServiceException.UnexpectedErrorException("Something went wrong setting the favorite", e);
            }
        }
    }

    public static String generateBlueUrl(String org, Item i) {
        String url = "/organizations/" + org + "/pipelines/";
        if(i instanceof WorkflowJob) {
            WorkflowJob job = (WorkflowJob)i;
            ItemGroup it = job.getParent();
            if(it instanceof WorkflowMultiBranchProject) {
                url += ((WorkflowMultiBranchProject) it).getName() + "/branches/" + job.getName();
            } else {
                url += job.getName();
            }
        } else {
            url += i.getName();
        }
        return url;
    }
}
