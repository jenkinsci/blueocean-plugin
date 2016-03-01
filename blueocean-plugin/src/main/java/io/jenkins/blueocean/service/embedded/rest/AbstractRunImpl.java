package io.jenkins.blueocean.service.embedded.rest;

import hudson.model.FreeStyleBuild;
import hudson.model.Run;
import io.jenkins.blueocean.rest.model.BlueRun;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;

import java.util.Date;

/**
 * Basic {@link BlueRun} implementation.
 *
 * @author Vivek Pandey
 */
public abstract class AbstractRunImpl extends BlueRun {
    protected abstract Run getRun();

    @Override
    public String getOrganization() {
        return OrganizationImpl.INSTANCE.getName();
    }

    @Override
    public String getId() {
        return getRun().getId();
    }

    @Override
    public String getPipeline() {
        return getRun().getParent().getName();
    }

    @Override
    public Status getStatus() {
        return getRun().getResult() != null ? Status.valueOf(getRun().getResult().toString()) : Status.UNKNOWN;
    }

    @Override
    public Date getStartTime() {
        return new Date(getRun().getStartTimeInMillis());
    }

    @Override
    public Date getEnQueueTime() {
        return new Date(getRun().getTimeInMillis());
    }

    @Override
    public Date getEndTime() {
        if (!getRun().isBuilding()) {
            return new Date(getRun().getStartTimeInMillis() + getRun().getDuration());
        }
        return null;
    }

    @Override
    public Long getDurationInMillis() {
        return getRun().getDuration();
    }

    @Override
    public abstract String getBranch();

    @Override
    public abstract String getCommitId();

    @Override
    public String getRunSummary() {
        return getRun().getBuildStatusSummary().message;
    }

    @Override
    public String getType() {
        return getRun().getClass().getSimpleName();
    }

    public static class BasicRunImpl extends AbstractRunImpl {
        private final Run run;

        public BasicRunImpl(Run run){
            this.run = run;
        }

        @Override
        protected Run getRun() {
            return run;
        }

        @Override
        public String getBranch() {
            return null;
        }

        @Override
        public String getCommitId() {
            return null;
        }
    }

    protected static BlueRun getBlueRun(Run r){
        //TODO: We need to take care several other job types
        if (r instanceof FreeStyleBuild) {
            return new FreeStyleRunImpl((FreeStyleBuild)r);
        }else if(r instanceof WorkflowRun){
            return new PipelineRunImpl((WorkflowRun)r);
        }else{
            return new BasicRunImpl(r);
        }
    }
}
