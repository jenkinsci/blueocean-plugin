package io.jenkins.blueocean.service.embedded.rest;

import com.google.common.collect.ImmutableList;
import edu.umd.cs.findbugs.annotations.NonNull;
import io.jenkins.blueocean.rest.hal.Link;
import io.jenkins.blueocean.rest.hal.Links;
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
import org.kohsuke.stapler.export.Exported;

import java.util.Collection;
import java.util.Date;

public class QueuedBlueRun extends BlueRun {

    public static final String QUEUE_ID = "queueId";

    private final BlueRunState runState;
    private final BlueRunResult runResult;
    private final QueueItemImpl item;
    private final Link parent;

    public QueuedBlueRun(BlueRunState runState, BlueRunResult runResult, QueueItemImpl item, Link parent) {
        this.runState = runState;
        this.runResult = runResult;
        this.item = item;
        this.parent = parent;
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
    public String getOrganization() {
        return item.getOrganization();
    }

    @Override
    public String getId() {
        return Integer.toString(item.getExpectedBuildNumber());
    }

    @Exported(name = QUEUE_ID)
    public String getQueueId() {
        return item.getId();
    }

    @Override
    public String getPipeline() {
        return item.getPipeline();
    }

    @Override
    public Date getStartTime() {
        return null;
    }

    @NonNull
    @Override
    public Container<BlueChangeSetEntry> getChangeSet() {
        return Containers.empty(getLink());
    }

    @Override
    public Date getEnQueueTime() {
        return item.getQueuedTime();
    }

    @Override
    public Date getEndTime() {
        return null;
    }

    @Override
    public Long getDurationInMillis() {
        return null;
    }

    @Override
    public Long getEstimatedDurtionInMillis() {
        return null;
    }

    @Override
    public BlueRunState getStateObj() {
        return runState;
    }

    @Override
    public BlueRunResult getResult() {
        return runResult;
    }

    @Override
    public String getRunSummary() {
        return null;
    }

    @Override
    public String getType() {
        return "QueuedItem";
    }

    @Override
    public BlueRun stop(Boolean blocking, Integer timeOutInSecs) {
        item.delete();
        return new QueuedBlueRun(BlueRunState.FINISHED, BlueRunResult.ABORTED, item, parent);
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
        return ImmutableList.of();
    }

    @Override
    public BluePipelineStepContainer getSteps() {
        return null;
    }

    @Override
    public Object getLog() {
        return null;
    }

    @Override
    public BlueRun replay() {
        return null;
    }

    @Override
    public boolean isReplayable() {
        return false;
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
    public BlueTestSummary getBlueTestSummary() {
        return null;
    }

    @Override
    public Collection<BlueCause> getCauses() {
        return item.getCauses();
    }

    @Override
    public String getCauseOfBlockage() {
        return item.getCauseOfBlockage();
    }

    @Override
    public Link getLink() {
        return parent.rel("runs/" + item.getExpectedBuildNumber());
    }

    @Override
    public Links getLinks() {
        return super.getLinks().add("parent", parent);
    }
}
