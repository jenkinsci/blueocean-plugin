package io.jenkins.blueocean.rest.impl.pipeline;

import com.cloudbees.hudson.plugins.folder.computed.ComputedFolder;
import hudson.Extension;
import hudson.Util;
import hudson.model.BuildableItem;
import hudson.model.Item;
import hudson.model.Job;
import io.jenkins.blueocean.commons.ServiceException;
import io.jenkins.blueocean.rest.Navigable;
import io.jenkins.blueocean.rest.Reachable;
import io.jenkins.blueocean.rest.annotation.Capability;
import io.jenkins.blueocean.rest.factory.BluePipelineFactory;
import io.jenkins.blueocean.rest.hal.Link;
import io.jenkins.blueocean.rest.model.BluePipeline;
import io.jenkins.blueocean.rest.model.BluePipelineScm;
import io.jenkins.blueocean.rest.model.Resource;
import jenkins.branch.MultiBranchProject;
import jenkins.scm.api.metadata.ObjectMetadataAction;
import jenkins.scm.api.metadata.PrimaryInstanceMetadataAction;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.export.ExportedBean;

import java.util.concurrent.ExecutionException;

import static io.jenkins.blueocean.rest.model.KnownCapabilities.BLUE_BRANCH;
import static io.jenkins.blueocean.rest.model.KnownCapabilities.JENKINS_WORKFLOW_JOB;
import static io.jenkins.blueocean.rest.model.KnownCapabilities.PULL_REQUEST;

/**
 * @author Vivek Pandey
 */
@Capability({BLUE_BRANCH, JENKINS_WORKFLOW_JOB, PULL_REQUEST})
public class BranchImpl extends PipelineImpl {

    private final Link parent;
    protected final Job job;

    public BranchImpl(Job job, Link parent) {
        super(job);
        this.job = job;
        this.parent = parent;
    }

    @Exported(name = PullRequest.PULL_REQUEST, inline = true, skipNull =  true)
    public PullRequest getPullRequest() {
        return PullRequest.get(job);
    }

    @Exported(name = Branch.BRANCH, inline = true)
    public Branch getBranch() {
        ObjectMetadataAction om = job.getAction(ObjectMetadataAction.class);
        PrimaryInstanceMetadataAction pima = job.getAction(PrimaryInstanceMetadataAction.class);
        String url = om != null && om.getObjectUrl() != null ? om.getObjectUrl() : null;
        return new Branch(url, pima != null);
    }

    @Override
    public Link getLink() {
        return parent.rel(Util.rawEncode(getName()));
    }

    @Navigable
    @Override
    public BluePipelineScm getScm() {
        if(job instanceof WorkflowJob && job.getParent() instanceof ComputedFolder) {
            return new ScmResourceImpl((ComputedFolder) job.getParent(), (BuildableItem) job,this);
        }else{
            return null;
        }
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

        public static final String BRANCH = "branch";
        public static final String BRANCH_URL = "url";
        public static final String BRANCH_PRIMARY = "isPrimary";

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

        public static Branch getBranch(final Job job) {
            try {
                return Caches.BRANCH_METADATA.get(job.getFullName());
            } catch (ExecutionException e) {
                throw new ServiceException.UnexpectedErrorException("loading branch metadata for '" + job.getFullName() + "'", e);
            }
        }
    }

    @ExportedBean
    public static class PullRequest {

        public static final String PULL_REQUEST = "pullRequest";
        public static final String PULL_REQUEST_NUMBER = "id";
        public static final String PULL_REQUEST_AUTHOR = "author";
        public static final String PULL_REQUEST_TITLE = "title";
        public static final String PULL_REQUEST_URL = "url";

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

        public static PullRequest get(final Job job) {
            try {
                return Caches.PULL_REQUEST_METADATA.get(job.getFullName());
            } catch (ExecutionException e) {
                throw new ServiceException.UnexpectedErrorException("loading pr metadata for '" + job.getFullName() + "'", e);
            }
        }
    }
}
