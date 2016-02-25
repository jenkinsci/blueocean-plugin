package io.jenkins.blueocean.service.embedded.rest;

import hudson.model.Run;
import io.jenkins.blueocean.rest.sandbox.BORun;

import java.util.Date;

/**
 * @author Vivek Pandey
 */
public class FreeStyleRun extends BORun {

    private final Run run;

    public FreeStyleRun(Run run) {
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
        return getStatusFromJenkinsRun(run);
    }

    @Override
    public RunTrend getRunTrend() {
        return null;
    }

    @Override
    public Date getStartTime() {
        return new Date(run.getStartTimeInMillis());
    }

    @Override
    public Date getEnQueueTime() {
        return null;
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

    private BORun.Status getStatusFromJenkinsRun(hudson.model.Run r){
        hudson.model.Result result = r.getResult();
        if(result == null){
            return BORun.Status.EXECUTING;
        }
        if (result == hudson.model.Result.SUCCESS) {
            return BORun.Status.SUCCESSFUL;
        } else if (result == hudson.model.Result.FAILURE || result == hudson.model.Result.UNSTABLE) {
            return BORun.Status.FAILING;
        } else if (!result.isCompleteBuild()) {
            return BORun.Status.EXECUTING;
        }else if(r.hasntStartedYet()){
            return BORun.Status.IN_QUEUE;
        }else if(result == hudson.model.Result.ABORTED){
            return BORun.Status.ABORTED;
        } else if (result == hudson.model.Result.NOT_BUILT){
            return BORun.Status.NOT_BUILT;
        }
        return BORun.Status.UNKNOWN;
    }
}
