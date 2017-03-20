package io.jenkins.blueocean.blueocean_github_pipeline;

import com.google.common.collect.Iterables;
import hudson.Extension;
import io.jenkins.blueocean.rest.Reachable;
import io.jenkins.blueocean.rest.hal.Link;
import io.jenkins.blueocean.rest.impl.pipeline.OrganizationFolderPipelineImpl;
import jenkins.branch.OrganizationFolder;
import jenkins.scm.api.SCMNavigator;
import org.apache.commons.lang3.StringUtils;
import org.jenkinsci.plugins.github_branch_source.GitHubSCMNavigator;

/**
 * @author Vivek Pandey
 */
public class GithubOrganizationFolder  extends OrganizationFolderPipelineImpl {
    public GithubOrganizationFolder(OrganizationFolder folder, Link parent) {
        super(folder, parent);
    }

    @Override
    public boolean isScanAllRepos() {
        if(!getFolder().getSCMNavigators().isEmpty()) {
            SCMNavigator scmNavigator = getFolder().getSCMNavigators().get(0);
            if(scmNavigator instanceof GitHubSCMNavigator){
                GitHubSCMNavigator gitHubSCMNavigator = (GitHubSCMNavigator) scmNavigator;
                return (StringUtils.isBlank(gitHubSCMNavigator.getIncludes()) || gitHubSCMNavigator.getIncludes().equals("*"))
                        && StringUtils.isBlank(gitHubSCMNavigator.getExcludes())
                        && (StringUtils.isBlank(gitHubSCMNavigator.getPattern())
                        || gitHubSCMNavigator.getPattern().equals(".*"));

            }
        }
        return super.isScanAllRepos();
    }

    @Extension(ordinal = -8)
    public static class OrganizationFolderFactoryImpl extends OrganizationFolderFactory {
        @Override
        protected OrganizationFolderPipelineImpl getFolder(jenkins.branch.OrganizationFolder folder, Reachable parent) {
            SCMNavigator navigator = Iterables.getFirst(folder.getNavigators(), null);
            return GitHubSCMNavigator.class.isInstance(navigator) ? new GithubOrganizationFolder(folder, parent.getLink()) : null;
        }
    }
}
