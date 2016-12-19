package io.jenkins.blueocean.rest.impl.pipeline;

import com.google.common.collect.Iterables;
import hudson.Extension;
import io.jenkins.blueocean.rest.Reachable;
import io.jenkins.blueocean.rest.hal.Link;
import jenkins.scm.api.SCMNavigator;
import org.jenkinsci.plugins.github_branch_source.GitHubSCMNavigator;

public class GithubOrganizationFolder extends OrganizationFolder {

    public GithubOrganizationFolder(jenkins.branch.OrganizationFolder folder, Link parent) {
        super(folder, parent);
    }

    @Extension
    public static class OrganizationFolderFactoryImpl extends OrganizationFolderFactory {
        @Override
        protected OrganizationFolder getFolder(jenkins.branch.OrganizationFolder folder, Reachable parent) {
            SCMNavigator navigator = Iterables.getFirst(folder.getNavigators(), null);
            return GitHubSCMNavigator.class.isInstance(navigator) ? new GithubOrganizationFolder(folder, parent.getLink()) : null;
        }
    }
}
