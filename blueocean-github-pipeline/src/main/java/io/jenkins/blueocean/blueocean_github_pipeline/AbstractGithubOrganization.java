package io.jenkins.blueocean.blueocean_github_pipeline;

import hudson.model.TopLevelItem;
import io.jenkins.blueocean.rest.impl.pipeline.scm.ScmOrganization;
import jenkins.branch.OrganizationFolder;
import jenkins.model.Jenkins;
import jenkins.scm.api.SCMNavigator;
import org.apache.http.client.utils.URIBuilder;
import org.jenkinsci.plugins.github_branch_source.GitHubSCMNavigator;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.net.URISyntaxException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Vivek Pandey
 */
public abstract class AbstractGithubOrganization extends ScmOrganization {

    private static final Logger LOGGER = Logger.getLogger(AbstractGithubOrganization.class.getName());

    private static final int AVATAR_SIZE = 50;

    @Override
    public boolean isJenkinsOrganizationPipeline() {
        for(TopLevelItem item: Jenkins.get().getItems()){
            if(item instanceof OrganizationFolder){
                OrganizationFolder folder = (OrganizationFolder) item;
                for(SCMNavigator navigator: folder.getNavigators()) {
                    if (navigator instanceof GitHubSCMNavigator) {
                        GitHubSCMNavigator scmNavigator = (GitHubSCMNavigator) navigator;
                        if(scmNavigator.getRepoOwner().equals(getName())){
                            return true;
                        }
                    }
                }
            }
        }
        return false;

    }

    @Nullable
    protected static String getAvatarWithSize(@Nonnull String avatarUrl) {
        try {
            return new URIBuilder(avatarUrl).addParameter("s", Integer.toString(AVATAR_SIZE)).build().toString();
        } catch (URISyntaxException e) {
            LOGGER.log(Level.WARNING, "Could not parse avatar URL <" + avatarUrl + ">", e);
            return null;
        }
    }
}
