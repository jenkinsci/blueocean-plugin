package io.jenkins.blueocean.freestyle;

import java.util.Collection;
import java.util.Date;

import io.jenkins.blueocean.rest.hal.Link;
import io.jenkins.blueocean.rest.model.BlueActionProxy;
import io.jenkins.blueocean.rest.model.BlueChangeSetEntry;
import io.jenkins.blueocean.rest.model.BluePipelineNodeContainer;
import io.jenkins.blueocean.rest.model.BluePipelineStepContainer;
import io.jenkins.blueocean.rest.model.BlueQueueItem;
import io.jenkins.blueocean.rest.model.BlueRun;
import io.jenkins.blueocean.rest.model.Container;

public class BlueFreestyleRun extends BlueRun {

    @Override
    public Link getLink() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Collection<BlueActionProxy> getActions() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Container<BlueArtifact> getArtifacts() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Container<BlueChangeSetEntry> getChangeSet() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Long getDurationInMillis() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Date getEnQueueTime() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Date getEndTime() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Long getEstimatedDurtionInMillis() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getId() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Object getLog() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public BluePipelineNodeContainer getNodes() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getOrganization() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getPipeline() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public BlueRunResult getResult() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getRunSummary() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Date getStartTime() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public BlueRunState getStateObj() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public BluePipelineStepContainer getSteps() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getType() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public BlueQueueItem replay() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public BlueRun stop() {
        // TODO Auto-generated method stub
        return null;
    }

}
