package io.jenkins.blueocean.rest.impl.pipeline;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import hudson.Extension;
import hudson.model.Item;
import hudson.model.Job;
import hudson.model.listeners.ItemListener;
import io.jenkins.blueocean.rest.impl.pipeline.BranchImpl.Branch;
import io.jenkins.blueocean.rest.impl.pipeline.BranchImpl.PullRequest;
import jenkins.model.Jenkins;
import jenkins.scm.api.SCMHead;
import jenkins.scm.api.metadata.ContributorMetadataAction;
import jenkins.scm.api.metadata.ObjectMetadataAction;
import jenkins.scm.api.metadata.PrimaryInstanceMetadataAction;
import jenkins.scm.api.mixin.ChangeRequestSCMHead;

import javax.annotation.Nonnull;
import java.util.concurrent.TimeUnit;

class Caches {

    public static final LoadingCache<String, PullRequest> PULL_REQUEST_METADATA = CacheBuilder.newBuilder().expireAfterAccess(1, TimeUnit.DAYS).build(new CacheLoader<String, PullRequest>() {
        @Override
        public PullRequest load(@Nonnull String key) throws Exception {
            Job job = Jenkins.getInstance().getItemByFullName(key, Job.class);
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
    });


    public static final LoadingCache<String, Branch> BRANCH_METADATA = CacheBuilder.newBuilder().expireAfterAccess(1, TimeUnit.DAYS).build(new CacheLoader<String, Branch>() {
        @Override
        public Branch load(@Nonnull String key) throws Exception {
            Job job = Jenkins.getInstance().getItemByFullName(key, Job.class);
            if (job == null) {
                return null;
            }
            ObjectMetadataAction om = job.getAction(ObjectMetadataAction.class);
            PrimaryInstanceMetadataAction pima = job.getAction(PrimaryInstanceMetadataAction.class);
            if (om == null && pima == null) {
                return null;
            }
            String url = om != null && om.getObjectUrl() != null ? om.getObjectUrl() : null;
            return new Branch(url, pima != null);
        }
    });

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

    private Caches() {}
}
