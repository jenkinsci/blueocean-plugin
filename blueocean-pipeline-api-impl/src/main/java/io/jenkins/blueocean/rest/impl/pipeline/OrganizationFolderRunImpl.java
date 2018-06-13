package io.jenkins.blueocean.rest.impl.pipeline;

import com.cloudbees.hudson.plugins.folder.computed.FolderComputation;
import hudson.model.CauseAction;
import hudson.model.Result;
import io.jenkins.blueocean.rest.Reachable;
import io.jenkins.blueocean.rest.hal.Link;
import io.jenkins.blueocean.rest.model.BlueActionProxy;
import io.jenkins.blueocean.rest.model.BlueArtifactContainer;
import io.jenkins.blueocean.rest.model.BlueChangeSetEntry;
import io.jenkins.blueocean.rest.model.BluePipelineNodeContainer;
import io.jenkins.blueocean.rest.model.BluePipelineStepContainer;
import io.jenkins.blueocean.rest.model.BlueRun;
import io.jenkins.blueocean.rest.model.BlueTestResultContainer;
import io.jenkins.blueocean.rest.model.BlueTestSummary;
import io.jenkins.blueocean.rest.model.Container;
import io.jenkins.blueocean.rest.model.Containers;
import io.jenkins.blueocean.service.embedded.rest.LogResource;
import io.jenkins.blueocean.service.embedded.rest.QueueItemImpl;
import org.kohsuke.stapler.QueryParameter;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.Date;

/**
 * @author Vivek Pandey
 */
public class OrganizationFolderRunImpl extends BlueRun {

    /*
     * OrganizationFolder can be only replayed once created. That means it has only one run, hence run id 1
     */
    static final String RUN_ID = "1";

    private final OrganizationFolderPipelineImpl pipeline;
    private final Link self;
    private final FolderComputation folderComputation;


    public OrganizationFolderRunImpl(OrganizationFolderPipelineImpl pipeline, Reachable parent) {
        this.pipeline = pipeline;
        this.folderComputation = pipeline.folder.getComputation();
        this.self = parent.getLink().rel(RUN_ID);
    }

    @Override
    public Link getLink() {
        return self;
    }

    @Override
    public String getOrganization() {
        return pipeline.getOrganizationName();
    }

    @Override
    public String getId() {
        return RUN_ID;
    }

    @Override
    public String getPipeline() {
        return pipeline.getName();
    }

    @Override
    public String getName() {
        return null;
    }

    @Override
    public String getDescription() {
        return null;
    }

    @Override
    public Date getStartTime() {
        return folderComputation.getTimestamp().getTime();
    }

    @Nonnull
    @Override
    public Container<BlueChangeSetEntry> getChangeSet() {
        return Containers.empty(getLink());
    }

    @Override
    public Date getEnQueueTime() {
        return getStartTime();
    }

    @Override
    public Date getEndTime() {
        return null;
    }

    @Override
    public String getStartTimeString() {
        return null;
    }

    @Override
    public String getEnQueueTimeString() {
        return null;
    }

    @Override
    public String getEndTimeString() {
        return null;
    }

    @Override
    public Long getDurationInMillis() {
        if(folderComputation.isBuilding()){
            return (System.currentTimeMillis() - folderComputation.getTimestamp().getTimeInMillis());
        }
        return getEstimatedDurtionInMillis(); //TODO: FolderComputation doesn't expose duration as date/time value. For now we return estimatedDuration. Raise this issue.

    }

    @Override
    public Long getEstimatedDurtionInMillis() {
        return folderComputation.getEstimatedDuration();
    }

    @Override
    public BlueRunState getStateObj() {
        return folderComputation.isBuilding()? BlueRunState.RUNNING : BlueRunState.FINISHED;
    }


    @Override
    public BlueRunResult getResult() {
        Result result = folderComputation.getResult();
        return result != null
                ? BlueRun.BlueRunResult.valueOf(result.toString())
                : BlueRunResult.UNKNOWN;
    }

    @Override
    public String getRunSummary() {
        return String.format("%s:%s",getResult(), getStateObj());
    }

    @Override
    public String getType() {
        return FolderComputation.class.getName();
    }

    @Override
    public BlueRun stop(@QueryParameter("blocking") Boolean blocking, @QueryParameter("timeOutInSecs") Integer timeOutInSecs) {
        return null;
    }

    @Override
    public String getArtifactsZipFile() {
        return null;
    }

    @Override
    public BlueArtifactContainer getArtifacts() {
        return null;
    }

    @Override
    public BluePipelineNodeContainer getNodes() {
        return null;
    }

    @Override
    public Collection<BlueActionProxy> getActions() {
        return null;
    }

    @Override
    public BluePipelineStepContainer getSteps() {
        return null;
    }

    @Override
    public BlueTestResultContainer getTests() {
        return null;
    }

    @Override
    public BlueTestSummary getTestSummary() {
        return null;
    }

    @Override
    public BlueTestSummary getBlueTestSummary()
    {
        return null;
    }

    @Override
    public Object getLog() {
        return new LogResource(folderComputation.getLogText());
    }

    @Override
    public Collection<BlueCause> getCauses() {
        return null;
    }

    @Override
    public String getCauseOfBlockage() {
        return null;
    }

    @Override
    public BlueRun replay() {
        if(isReplayable()) {
            return new QueueItemImpl(this.pipeline.getOrganization(), pipeline.folder.scheduleBuild2(0,new CauseAction(new hudson.model.Cause.UserIdCause())), pipeline, 1).toRun();
        }
        return null;
    }

    @Override
    public boolean isReplayable() {
        return pipeline.folder.isBuildable();
    }
}
