package io.jenkins.blueocean.blueocean_github_pipeline;

import hudson.model.TopLevelItem;
import io.jenkins.blueocean.rest.impl.pipeline.scm.ScmOrganization;
import jenkins.branch.OrganizationFolder;
import jenkins.model.Jenkins;
import jenkins.scm.api.SCMNavigator;
import org.jenkinsci.plugins.github_branch_source.GitHubSCMNavigator;

/**
 * @author Vivek Pandey
 */
public abstract class AbstractGithubOrganization extends ScmOrganization {

    @Override
    public boolean isJenkinsOrganizationPipeline() {
        for(TopLevelItem item: Jenkins.getInstance().getItems()){
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
}
