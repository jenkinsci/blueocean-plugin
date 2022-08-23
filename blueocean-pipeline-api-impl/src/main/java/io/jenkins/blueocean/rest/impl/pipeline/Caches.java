package io.jenkins.blueocean.rest.impl.pipeline;

import com.github.benmanes.caffeine.cache.CacheLoader;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import hudson.Extension;
import hudson.model.Item;
import hudson.model.ItemGroup;
import hudson.model.Job;
import hudson.model.listeners.ItemListener;
import io.jenkins.blueocean.rest.factory.BlueIssueFactory;
import io.jenkins.blueocean.rest.impl.pipeline.BranchImpl.Branch;
import io.jenkins.blueocean.rest.impl.pipeline.BranchImpl.PullRequest;
import jenkins.branch.BranchProjectFactory;
import jenkins.branch.MultiBranchProject;
import jenkins.model.Jenkins;
import jenkins.scm.api.SCMHead;
import jenkins.scm.api.metadata.ContributorMetadataAction;
import jenkins.scm.api.metadata.ObjectMetadataAction;
import jenkins.scm.api.metadata.PrimaryInstanceMetadataAction;
import jenkins.scm.api.mixin.ChangeRequestSCMHead;
import org.apache.commons.lang3.StringUtils;

import edu.umd.cs.findbugs.annotations.Nullable;
import java.util.concurrent.TimeUnit;

class Caches {

    /**
     * Pull request metadata cache maximum number of entries. Default 10000.
     */
    static final long PR_METADATA_CACHE_MAX_SIZE = Long.getLong("PR_METADATA_CACHE_MAX_SIZE", 10000);

    /**
     * Branch metadata cache maximum number of entries. Default 10000.
     */
    static final long BRANCH_METADATA_CACHE_MAX_SIZE = Long.getLong("BRANCH_METADATA_CACHE_MAX_SIZE", 10000);

    static final LoadingCache<String, PullRequest> PULL_REQUEST_METADATA = Caffeine.newBuilder()
            .maximumSize(PR_METADATA_CACHE_MAX_SIZE)
            .expireAfterAccess(1, TimeUnit.DAYS)
            .build(new PullRequestCacheLoader(null));


    static final LoadingCache<String, Branch> BRANCH_METADATA = Caffeine.newBuilder()
            .maximumSize(BRANCH_METADATA_CACHE_MAX_SIZE)
            .expireAfterAccess(1, TimeUnit.DAYS)
            .build(new BranchCacheLoader(null));

    @Extension
    public static class ListenerImpl extends ItemListener {

        @Override
        public void onLocationChanged(Item item, String oldFullName, String newFullName) {
            if (!(item instanceof Job)) {
                return;
            }
            PULL_REQUEST_METADATA.invalidate(oldFullName);
            BRANCH_METADATA.invalidate(oldFullName);

            PULL_REQUEST_METADATA.refresh(newFullName);
            BRANCH_METADATA.refresh(newFullName);
        }

        @Override
        public void onUpdated(Item item) {
            if (!(item instanceof Job)) {
                return;
            }
            PULL_REQUEST_METADATA.refresh(item.getFullName());
            BRANCH_METADATA.refresh(item.getFullName());
        }

        @Override
        public void onDeleted(Item item) {
            if (!(item instanceof Job)) {
                return;
            }
            PULL_REQUEST_METADATA.invalidate(item.getFullName());
            BRANCH_METADATA.invalidate(item.getFullName());
        }
    }

    static class BranchCacheLoader implements CacheLoader<String, Branch>
    {
        private Jenkins jenkins;


        BranchCacheLoader(@Nullable Jenkins jenkins) {
            this.jenkins = jenkins;
        }

        @Override
        public Branch load(String key) throws Exception {
            Jenkins jenkins = this.jenkins!=null?this.jenkins:Jenkins.get();
            Job job = jenkins.getItemByFullName(key, Job.class);
            if (job == null) {
                return null;
            }
            ObjectMetadataAction om = job.getAction(ObjectMetadataAction.class);
            PrimaryInstanceMetadataAction pima = job.getAction(PrimaryInstanceMetadataAction.class);
            String url = om != null && om.getObjectUrl() != null ? om.getObjectUrl() : null;
            if (StringUtils.isEmpty(url)) {
                /*
                 * Borrowed from https://github.com/jenkinsci/branch-api-plugin/blob/c4d394415cf25b6890855a08360119313f1330d2/src/main/java/jenkins/branch/BranchNameContributor.java#L63
                 * for those that don't implement object metadata action
                 */
                ItemGroup parent = job.getParent();
                if (parent instanceof MultiBranchProject) {
                    BranchProjectFactory projectFactory = ((MultiBranchProject) parent).getProjectFactory();
                    if (projectFactory.isProject(job)) {
                        SCMHead head = projectFactory.getBranch(job).getHead();
                        url = head.getName();
                    }
                }
            }
            if (StringUtils.isEmpty(url) && pima == null) {
                return null;
            }
            return new Branch(url, pima != null, BlueIssueFactory.resolve(job));
        }
    }

    static class PullRequestCacheLoader implements CacheLoader<String, PullRequest>
    {
        private Jenkins jenkins;

        PullRequestCacheLoader(@Nullable Jenkins jenkins) {
            this.jenkins = jenkins;
        }

        @Override
        public PullRequest load(String key) throws Exception {
            Jenkins jenkins = this.jenkins!=null?this.jenkins:Jenkins.get();
            Job job = jenkins.getItemByFullName(key, Job.class);
            if (job == null) {
                return null;
            }
            // TODO probably want to be using SCMHeadCategory instances to categorize them instead of hard-coding for PRs
            SCMHead head = SCMHead.HeadByItem.findHead(job);
            if (head instanceof ChangeRequestSCMHead) {
                ChangeRequestSCMHead cr = (ChangeRequestSCMHead) head;
                ObjectMetadataAction om = job.getAction(ObjectMetadataAction.class);
                ContributorMetadataAction cm = job.getAction(ContributorMetadataAction.class);
                return new PullRequest(
                        cr.getId(),
                        om != null ? om.getObjectUrl() : null,
                        om != null ? om.getObjectDisplayName() : null,
                        cm != null ? cm.getContributor() : null
                    );
            }
            return null;
        }
    }

    private Caches() {}
}
