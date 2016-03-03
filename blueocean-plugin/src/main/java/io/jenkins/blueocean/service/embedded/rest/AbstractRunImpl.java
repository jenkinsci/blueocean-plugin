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
    private final Run run;

    public AbstractRunImpl(Run run) {
        this.run = run;
    }
    @Override
    public String getOrganization() {
        return OrganizationImpl.INSTANCE.getName();
    }

    @Override
    public String getId() {
        return run.getId();
    }

    @Override
    public String getPipeline() {
        return run.getParent().getName();
    }

    @Override
    public Status getStatus() {
        return run.getResult() != null ? Status.valueOf(run.getResult().toString()) : Status.UNKNOWN;
    }

    @Override
    public Date getStartTime() {
        return new Date(run.getStartTimeInMillis());
    }

    @Override
    public Date getEnQueueTime() {
        return new Date(run.getTimeInMillis());
    }

    @Override
    public Date getEndTime() {
        if (!run.isBuilding()) {
            return new Date(run.getStartTimeInMillis() + run.getDuration());
        }
        return null;
    }

    @Override
    public Long getDurationInMillis() {
        return run.getDuration();
    }

    @Override
    public String getBranch() {
        return null;
    }

    @Override
    public String getCommitId() {
        return null;
    }

    @Override
    public String getRunSummary() {
        return run.getBuildStatusSummary().message;
    }

    @Override
    public String getType() {
        return run.getClass().getSimpleName();
    }

    @Override
    public Object getLog() {
        return new LogResource(run);
    }

    public static class BasicRunImpl extends AbstractRunImpl {
        public BasicRunImpl(Run run) {
            super(run);
        }
    }

    protected static BlueRun getBlueRun(Run r){
        //TODO: We need to take care several other job types
        if (r.getClass().getSimpleName().equals(FreeStyleBuild.class.getSimpleName())) {
            return new FreeStyleRunImpl(r);
        }else if(r.getClass().getSimpleName().equals(WorkflowRun.class.getSimpleName())){
            return new PipelineRunImpl(r);
        }else{
            return new BasicRunImpl(r);
        }
    }
}
