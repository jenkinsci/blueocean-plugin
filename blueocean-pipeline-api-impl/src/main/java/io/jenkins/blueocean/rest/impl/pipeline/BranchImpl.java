package io.jenkins.blueocean.rest.impl.pipeline;

import com.cloudbees.hudson.plugins.folder.computed.ComputedFolder;
import hudson.Extension;
import hudson.Util;
import hudson.model.BuildableItem;
import hudson.model.Item;
import hudson.model.Job;
import io.jenkins.blueocean.pipeline.api.BlueMultiBranchPipeline.Branch;
import io.jenkins.blueocean.pipeline.api.BlueMultiBranchPipeline.PullRequest;
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
}
