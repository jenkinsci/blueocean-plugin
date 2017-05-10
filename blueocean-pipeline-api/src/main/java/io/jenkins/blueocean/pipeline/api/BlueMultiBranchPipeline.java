package io.jenkins.blueocean.pipeline.api;

import hudson.model.Job;
import io.jenkins.blueocean.commons.ServiceException;
import io.jenkins.blueocean.rest.annotation.Capability;
import io.jenkins.blueocean.rest.hal.Link;
import io.jenkins.blueocean.rest.model.BluePipelineContainer;
import io.jenkins.blueocean.rest.model.BluePipelineFolder;
import io.jenkins.blueocean.rest.model.BlueRun;
import io.jenkins.blueocean.rest.model.BlueRunContainer;
import jenkins.scm.api.SCMHead;
import jenkins.scm.api.metadata.ContributorMetadataAction;
import jenkins.scm.api.metadata.ObjectMetadataAction;
import jenkins.scm.api.metadata.PrimaryInstanceMetadataAction;
import jenkins.scm.api.mixin.ChangeRequestSCMHead;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.export.ExportedBean;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;

import static io.jenkins.blueocean.rest.model.KnownCapabilities.BLUE_MULTI_BRANCH_PIPELINE;

/**
 * Multi-branch pipeline model
 *
 * @author Vivek Pandey
 */
@Capability(BLUE_MULTI_BRANCH_PIPELINE)
public abstract class BlueMultiBranchPipeline extends BluePipelineFolder {
    public static final String TOTAL_NUMBER_OF_BRANCHES="totalNumberOfBranches";
    public static final String NUMBER_OF_FAILING_BRANCHES="numberOfFailingBranches";
    public static final String NUMBER_OF_SUCCESSFULT_BRANCHES="numberOfSuccessfulBranches";
    public static final String TOTAL_NUMBER_OF_PULL_REQUESTS="totalNumberOfPullRequests";
    public static final String NUMBER_OF_FAILING_PULL_REQUESTS="numberOfFailingPullRequests";
    public static final String NUMBER_OF_SUCCESSFULT_PULL_REQUESTS="numberOfSuccessfulPullRequests";
    public static final String BRANCH_NAMES ="branchNames";

    /**
     * @return total number of branches
     */
    @Exported(name = TOTAL_NUMBER_OF_BRANCHES)
    public abstract int  getTotalNumberOfBranches();

    /**
     * @return total number of failing branches
     */
    @Exported(name = NUMBER_OF_FAILING_BRANCHES)
    public abstract int getNumberOfFailingBranches();

    /**
     * @return total number of successful branches
     */
    @Exported(name = NUMBER_OF_SUCCESSFULT_BRANCHES)
    public abstract int getNumberOfSuccessfulBranches();

    /**
    * @return total number of pull requests
    */
    @Exported(name = TOTAL_NUMBER_OF_PULL_REQUESTS)
    public abstract int  getTotalNumberOfPullRequests();
    /**
     * @return total number of pull requests
     */
    @Exported(name = NUMBER_OF_FAILING_PULL_REQUESTS)
    public abstract int getNumberOfFailingPullRequests();

    /**
     * @return total number of pull requests
     */
    @Exported(name = NUMBER_OF_SUCCESSFULT_PULL_REQUESTS)
    public abstract int getNumberOfSuccessfulPullRequests();

    /**
     * @return Gives {@link BluePipelineContainer}
     */
    public abstract BluePipelineContainer getBranches();

    /**
     * @return Gives array of branch names
     */
    @Exported(name = BRANCH_NAMES)
    public abstract Collection<String> getBranchNames();

    /**
     * MultiBranch pipeline is computed folder, no sub-folders in it
     */
    @Override
    public Iterable<String> getPipelineFolderNames() {
        return Collections.emptyList();
    }

    /**
     * @return It gives no-op {@link BlueRunContainer} since Multi-branch is not a build item, does not build on its own
     *
     */
    public BlueRunContainer getRuns(){
        return new BlueRunContainer() {
            @Override
            public Link getLink() {
                return null;
            }

            @Override
            public BlueRun get(String name) {
                throw new ServiceException.NotFoundException(
                    String.format("It is multi-branch project. No run with name: %s found.", name));
            }

            @Override
            public Iterator<BlueRun> iterator() {
                return Collections.emptyIterator();
            }

            @Override
            public BlueRun create(StaplerRequest request) {
                throw new ServiceException.NotImplementedException("This action is not supported");
            }
        };
    }

    @ExportedBean
    public static class Branch {

        public static final String BRANCH = "branch";
        private static final String BRANCH_URL = "url";
        private static final String BRANCH_PRIMARY = "isPrimary";

        private final String url;
        private final boolean primary;

        public Branch(String url, boolean primary) {
            this.url = url;
            this.primary = primary;
        }

        @Exported(name = BRANCH_URL)
        public String getUrl() {
            return url;
        }

        @Exported(name = BRANCH_PRIMARY)
        public boolean isPrimary() {
            return primary;
        }

        public static Branch getBranch(Job job) {
            ObjectMetadataAction om = job.getAction(ObjectMetadataAction.class);
            PrimaryInstanceMetadataAction pima = job.getAction(PrimaryInstanceMetadataAction.class);
            if (om == null && pima == null) {
                return null;
            }
            String url = om != null && om.getObjectUrl() != null ? om.getObjectUrl() : null;
            return new Branch(url, pima != null);
        }
    }

    @ExportedBean
    public static class PullRequest {

        public static final String PULL_REQUEST = "pullRequest";
        private static final String PULL_REQUEST_NUMBER = "id";
        private static final String PULL_REQUEST_AUTHOR = "author";
        private static final String PULL_REQUEST_TITLE = "title";
        private static final String PULL_REQUEST_URL = "url";

        private final String id;

        private final String url;

        private final String title;

        private final String author;

        public PullRequest(String id, String url, String title, String author) {
            this.id = id;
            this.url = url;
            this.title = title;
            this.author = author;
        }

        @Exported(name = PULL_REQUEST_NUMBER)
        public String getId() {
            return id;
        }


        @Exported(name = PULL_REQUEST_URL)
        public String getUrl() {
            return url;
        }


        @Exported(name = PULL_REQUEST_TITLE)
        public String getTitle() {
            return title;
        }


        @Exported(name = PULL_REQUEST_AUTHOR)
        public String getAuthor() {
            return author;
        }

        public static PullRequest get(Job job) {
            // TODO probably want to be using SCMHeadCategory instances to categorize them instead of hard-coding for PRs
            SCMHead head = SCMHead.HeadByItem.findHead(job);
            if(head instanceof ChangeRequestSCMHead) {
                ChangeRequestSCMHead cr = (ChangeRequestSCMHead)head;
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
}
