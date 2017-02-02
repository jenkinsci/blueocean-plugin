package io.jenkins.blueocean.rest.impl.pipeline;

import hudson.Extension;
import hudson.Util;
import hudson.model.Item;
import hudson.model.Job;
import io.jenkins.blueocean.rest.Reachable;
import io.jenkins.blueocean.rest.annotation.Capability;
import io.jenkins.blueocean.rest.hal.Link;
import io.jenkins.blueocean.rest.model.BluePipeline;
import io.jenkins.blueocean.rest.model.Resource;
import io.jenkins.blueocean.service.embedded.rest.BluePipelineFactory;
import jenkins.branch.MultiBranchProject;
import jenkins.scm.api.SCMHead;
import jenkins.scm.api.actions.ChangeRequestAction;
import jenkins.scm.api.metadata.ObjectMetadataAction;
import jenkins.scm.api.metadata.PrimaryInstanceMetadataAction;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.export.ExportedBean;

import static io.jenkins.blueocean.rest.model.KnownCapabilities.BLUE_BRANCH;
import static io.jenkins.blueocean.rest.model.KnownCapabilities.JENKINS_WORKFLOW_JOB;
import static io.jenkins.blueocean.rest.model.KnownCapabilities.PULL_REQUEST;

/**
 * @author Vivek Pandey
 */
@Capability({BLUE_BRANCH, JENKINS_WORKFLOW_JOB, PULL_REQUEST})
public class BranchImpl extends PipelineImpl {

    private static final String PULL_REQUEST = "pullRequest";
    private static final String BRANCH = "branch";

    private final Link parent;
    protected final Job job;

    public BranchImpl(Job job, Link parent) {
        super(job);
        this.job = job;
        this.parent = parent;
    }

    @Exported(name = PULL_REQUEST, inline = true)
    public PullRequest getPullRequest() {
        SCMHead head = SCMHead.HeadByItem.findHead(job);
        if(head != null) {
            ChangeRequestAction action = head.getAction(ChangeRequestAction.class);
            if(action != null){
                return new PullRequest(action.getId(), action.getURL().toExternalForm(), action.getTitle(), action.getAuthor());
            }
        }
        return null;
    }

    @Exported(name = BRANCH)
    public Branch getBranch() {
        ObjectMetadataAction om = job.getAction(ObjectMetadataAction.class);
        PrimaryInstanceMetadataAction pima = job.getAction(PrimaryInstanceMetadataAction.class);
        if (om == null && pima == null) {
            return null;
        }
        String url = om != null && om.getObjectUrl() != null ? om.getObjectUrl() : null;
        return new Branch(url, pima != null);
    }

    @Override
    public Link getLink() {
        return parent.rel(Util.rawEncode(getName()));
    }

    @Extension(ordinal = 4)
    public static class PipelineFactoryImpl extends BluePipelineFactory {

        @Override
        public BluePipeline getPipeline(Item item, Reachable parent) {
            if (item instanceof WorkflowJob && item.getParent() instanceof MultiBranchProject) {
                return new BranchImpl((Job) item, parent.getLink());
            }
            return null;
        }

        @Override
        public Resource resolve(Item context, Reachable parent, Item target) {
            if (context==target.getParent()) {
                return getPipeline(context,parent);
            }
            return null;
        }
    }

    @ExportedBean
    public static class Branch {
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
    }

    public static class PullRequest {
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
    }

}
