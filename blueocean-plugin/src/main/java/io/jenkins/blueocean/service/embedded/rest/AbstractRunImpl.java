package io.jenkins.blueocean.service.embedded.rest;

import com.fasterxml.jackson.annotation.JsonProperty;
import hudson.model.FreeStyleBuild;
import hudson.model.Run;
import hudson.plugins.git.util.BuildData;
import io.jenkins.blueocean.rest.model.BlueRun;
import io.jenkins.blueocean.rest.model.Container;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;
import org.kohsuke.stapler.export.Exported;

import java.util.Date;

/**
 * Basic {@link BlueRun} implementation.
 *
 * @author Vivek Pandey
 */
public class AbstractRunImpl<T extends Run> extends BlueRun {
    protected final T run;

    public AbstractRunImpl(T run) {
        this.run = run;
    }

    //TODO: It serializes jenkins Run model children, enable this code after fixing it
//    /**
//     * Allow properties reachable through {@link Run} to be exposed upon request (via the tree parameter).
//     */
//    @Exported
//    public T getRun() {
//        return run;
//    }

    /**
     * Subtype should return
     */
    public Container<?> getChangeSet() {
        return null;
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
    public Date getStartTime() {
        return new Date(run.getStartTimeInMillis());
    }

    @Override
    public Date getEnQueueTime() {
        return new Date(run.getTimeInMillis());
    }

    @Override
    public BlueRunState getStateObj() {
        if(!run.hasntStartedYet() && run.isLogUpdated()) {
            return BlueRunState.RUNNING;
        } else if(!run.isLogUpdated()){
            return BlueRunState.FINISHED;
        } else {
            return BlueRunState.QUEUED;
        }
    }

    @Override
    public BlueRunResult getResult() {
        return run.getResult() != null ? BlueRunResult.valueOf(run.getResult().toString()) : BlueRunResult.UNKNOWN;
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
    public String getRunSummary() {
        return run.getBuildStatusSummary().message;
    }

    @Override
    public String getType() {
        return run.getClass().getSimpleName();
    }

    @Override
    public Object getLog() {
        return new LogResource(run.getLogText());
    }

    protected static BlueRun getBlueRun(Run r){
        //TODO: We need to take care several other job types
        if (r instanceof FreeStyleBuild) {
            return new FreeStyleRunImpl((FreeStyleBuild)r);
        }else if(r instanceof WorkflowRun){
            return new PipelineRunImpl((WorkflowRun)r);
        }else{
            return new AbstractRunImpl<>(r);
        }
    }

    @Exported(name = "commitId")
    @JsonProperty("commitId")
    public String getCommitId(){
        BuildData data = run.getAction(BuildData.class);

        if(data == null){
            return null;
        } else {
            return data.getLastBuiltRevision().getSha1String();
        }
    }
}
