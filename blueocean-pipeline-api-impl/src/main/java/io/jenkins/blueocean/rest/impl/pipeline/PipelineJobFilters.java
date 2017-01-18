package io.jenkins.blueocean.rest.impl.pipeline;

import java.util.ArrayList;
import java.util.List;

import com.cloudbees.hudson.plugins.folder.Folder;
import com.google.common.base.Predicate;

import hudson.Extension;
import hudson.model.Item;
import hudson.model.ItemGroup;
import hudson.model.Job;
import io.jenkins.blueocean.service.embedded.rest.ContainerFilter;
import jenkins.branch.OrganizationFolder;
import jenkins.scm.api.SCMHead;
import jenkins.scm.api.actions.ChangeRequestAction;

public class PipelineJobFilters {
    private static boolean isPullRequest(Item item) {
        SCMHead head = SCMHead.HeadByItem.findHead(item);
        return head != null && head.getAction(ChangeRequestAction.class) != null;
    }

    @Extension
    public static class FolderJobFilter extends ContainerFilter {
        private final Predicate<Item> filter = new Predicate<Item>() {
            @Override
            public boolean apply(Item job) {
                if (Folder.class.equals(job.getClass()) // some subclasses are fine
                        || job instanceof OrganizationFolder) {
                    return false;
                }
                return true;
            }
        };
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
        private final Predicate<Item> filter = new Predicate<Item>() {
            @Override
            public boolean apply(Item job) {
                if (!isPullRequest(job)) {
                    return true;
                }
                return false;
            }
        };
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
        private final Predicate<Item> filter = new Predicate<Item>() {
            @Override
            public boolean apply(Item job) {
                if (isPullRequest(job)) {
                    return true;
                }
                return false;
            }
        };
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
