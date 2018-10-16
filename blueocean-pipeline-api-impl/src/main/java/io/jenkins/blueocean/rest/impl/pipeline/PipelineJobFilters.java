package io.jenkins.blueocean.rest.impl.pipeline;

import com.cloudbees.hudson.plugins.folder.Folder;
import com.google.common.base.Predicate;
import hudson.Extension;
import hudson.model.Item;
import io.jenkins.blueocean.service.embedded.rest.ContainerFilter;
import jenkins.branch.OrganizationFolder;
import jenkins.scm.api.SCMHead;
import jenkins.scm.api.mixin.ChangeRequestSCMHead;

public class PipelineJobFilters {
    public static boolean isPullRequest(Item item) {
        // TODO probably want to be using SCMHeadCategory instances to categorize them instead of hard-coding for PRs
        return SCMHead.HeadByItem.findHead(item) instanceof ChangeRequestSCMHead;
    }

    @Extension
    public static class FolderJobFilter extends ContainerFilter {
        private final Predicate<Item> filter = job -> !(Folder.class.equals(job.getClass()) || job instanceof OrganizationFolder);
        @Override
        public String getName() {
            return "no-folders";
        }
        @Override
        public Predicate<Item> getFilter() {
            return filter;
        }
    }

    @Extension
    public static class OriginFilter extends ContainerFilter {
        private final Predicate<Item> filter = job -> !isPullRequest(job);

        @Override
        public String getName() {
            return "origin";
        }
        @Override
        public Predicate<Item> getFilter() {
            return filter;
        }
    }

    @Extension
    public static class PullRequestFilter extends ContainerFilter {
        private final Predicate<Item> filter = job ->  isPullRequest(job);

        @Override
        public String getName() {
            return "pull-requests";
        }
        @Override
        public Predicate<Item> getFilter() {
            return filter;
        }
    }
}
