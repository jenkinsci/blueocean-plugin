package io.jenkins.blueocean.rest.impl.pipeline;

import com.cloudbees.hudson.plugins.folder.computed.ComputedFolder;
import hudson.Extension;
import hudson.model.BuildableItem;
import hudson.model.Item;
import hudson.model.Job;
import io.jenkins.blueocean.commons.ServiceException;
import io.jenkins.blueocean.rest.Navigable;
import io.jenkins.blueocean.rest.Reachable;
import io.jenkins.blueocean.rest.annotation.Capability;
import io.jenkins.blueocean.rest.factory.BluePipelineFactory;
import io.jenkins.blueocean.rest.hal.Link;
import io.jenkins.blueocean.rest.model.BlueIssue;
import io.jenkins.blueocean.rest.model.BlueOrganization;
import io.jenkins.blueocean.rest.model.BluePipeline;
import io.jenkins.blueocean.rest.model.BluePipelineScm;
import io.jenkins.blueocean.rest.model.BlueTrendContainer;
import io.jenkins.blueocean.rest.model.Resource;
import io.jenkins.blueocean.service.embedded.rest.BlueTrendContainerImpl;
import jenkins.branch.MultiBranchProject;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.export.ExportedBean;

import javax.annotation.CheckForNull;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Collection;
import java.util.concurrent.ExecutionException;

import static io.jenkins.blueocean.rest.model.KnownCapabilities.*;

/**
 * @author Vivek Pandey
 */
@Capability({BLUE_BRANCH, JENKINS_WORKFLOW_JOB, PULL_REQUEST})
public class BranchImpl extends PipelineImpl {

    private final Link parent;
    protected final Job job;

    public BranchImpl(BlueOrganization org, Job job, Link parent) {
        super(org, job);
        this.job = job;
        this.parent = parent;
    }

    @Exported(name = PullRequest.PULL_REQUEST, inline = true, skipNull =  true)
    @CheckForNull
    public PullRequest getPullRequest() {
        return PullRequest.get(job);
    }

    @Exported(name = Branch.BRANCH, inline = true)
    @CheckForNull
    public Branch getBranch() {
        return Branch.getBranch(job);
    }

    @Override
    public Link getLink() {
        try {
            return parent.rel(URLEncoder.encode(getName(), "UTF-8").replace("+", "%20"));
        } catch (UnsupportedEncodingException e) {
            return parent.rel(URLEncoder.encode(getName()).replace("+", "%20"));
        }
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

    @Override
    public BlueTrendContainer getTrends() {
        return new BlueTrendContainerImpl(this);
    }

    @Extension(ordinal = 4)
    public static class PipelineFactoryImpl extends BluePipelineFactory {

        @Override
        public BluePipeline getPipeline(Item item, Reachable parent, BlueOrganization organization) {
            if (item instanceof WorkflowJob && item.getParent() instanceof MultiBranchProject) {
                return new BranchImpl(organization, (Job) item, parent.getLink());
            }
            return null;
        }

        @Override
        public Resource resolve(Item context, Reachable parent, Item target, BlueOrganization organization) {
            if (context==target.getParent()) {
                return getPipeline(context, parent, organization);
            }
            return null;
        }
    }

    @ExportedBean
    public static class Branch {

        public static final String BRANCH = "branch";
        public static final String BRANCH_URL = "url";
        public static final String BRANCH_PRIMARY = "isPrimary";
        public static final String ISSUES = "issues";

        private final String url;
        private final boolean primary;
        private final Collection<BlueIssue> issues;

        public Branch(String url, boolean primary, Collection<BlueIssue> issues) {
            this.url = url;
            this.primary = primary;
            this.issues = issues;
        }

        @Exported(name = BRANCH_URL)
        public String getUrl() {
            return url;
        }

        @Exported(name = BRANCH_PRIMARY)
        public boolean isPrimary() {
            return primary;
        }

        @Exported(name = ISSUES, skipNull = true, inline = true)
        public Collection<BlueIssue> getIssues() {
            return issues;
        }

        public static Branch getBranch(final Job job) {
            try {
                return Caches.BRANCH_METADATA.get(job.getFullName()).orNull();
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
                return Caches.PULL_REQUEST_METADATA.get(job.getFullName()).orNull();
            } catch (ExecutionException e) {
                throw new ServiceException.UnexpectedErrorException("loading pr metadata for '" + job.getFullName() + "'", e);
            }
        }
    }
}
