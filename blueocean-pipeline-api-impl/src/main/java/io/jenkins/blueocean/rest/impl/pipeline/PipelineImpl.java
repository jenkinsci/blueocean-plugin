package io.jenkins.blueocean.rest.impl.pipeline;

import hudson.Extension;
import hudson.model.Item;
import hudson.model.Job;
import hudson.model.Run;
import io.jenkins.blueocean.rest.Reachable;
import io.jenkins.blueocean.rest.annotation.Capability;
import io.jenkins.blueocean.rest.factory.BluePipelineFactory;
import io.jenkins.blueocean.rest.factory.organization.OrganizationFactory;
import io.jenkins.blueocean.rest.model.BlueChangeSetEntry;
import io.jenkins.blueocean.rest.model.BlueOrganization;
import io.jenkins.blueocean.rest.model.BluePipeline;
import io.jenkins.blueocean.rest.model.BlueRun;
import io.jenkins.blueocean.rest.model.BlueTrendContainer;
import io.jenkins.blueocean.rest.model.Container;
import io.jenkins.blueocean.rest.model.Resource;
import io.jenkins.blueocean.service.embedded.rest.AbstractBlueRunSummary;
import io.jenkins.blueocean.service.embedded.rest.AbstractPipelineImpl;
import io.jenkins.blueocean.service.embedded.rest.BlueTrendContainerImpl;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;
import static io.jenkins.blueocean.rest.model.KnownCapabilities.JENKINS_WORKFLOW_JOB;
import static io.jenkins.blueocean.rest.model.KnownCapabilities.JENKINS_WORKFLOW_RUN;

/**
 * @author Kohsuke Kawaguchi
 */
@Capability({JENKINS_WORKFLOW_JOB})
public class PipelineImpl extends AbstractPipelineImpl {
    private final Job job;
    protected PipelineImpl(BlueOrganization organization, Job job) {
        super(organization, job);
        this.job = job;
    }

    @Override
    public BlueTrendContainer getTrends() {
        return new BlueTrendContainerImpl(this);
    }

    @Override
    public BlueRun getLatestRun() {
        Run run = job.getLastBuild();
        if(run instanceof WorkflowRun){
            BlueRun blueRun = new PipelineRunImpl((WorkflowRun) run, this, organization);
            return new PipelineRunSummary(blueRun, run, this, organization);
        }
        return super.getLatestRun();
    }

    @Extension(ordinal = 1)
    public static class PipelineFactoryImpl extends BluePipelineFactory {

        @Override
        public BluePipeline getPipeline(Item item, Reachable parent, BlueOrganization organization) {
            BlueOrganization org = OrganizationFactory.getInstance().getContainingOrg(item);
            if (org != null && item instanceof WorkflowJob) {
                return new PipelineImpl(org, (Job) item);
            }
            return null;
        }

        @Override
        public Resource resolve(Item context, Reachable parent, Item target, BlueOrganization organization) {
            if(context == target && target instanceof WorkflowJob) {
                return getPipeline(target, parent, organization);
            }

            return null;
        }
    }

    @Capability(JENKINS_WORKFLOW_RUN)
    static class PipelineRunSummary extends AbstractBlueRunSummary {
        public PipelineRunSummary(BlueRun blueRun, Run run, Reachable parent, BlueOrganization organization) {
            super(blueRun, run, parent, organization);
        }
    }
}
