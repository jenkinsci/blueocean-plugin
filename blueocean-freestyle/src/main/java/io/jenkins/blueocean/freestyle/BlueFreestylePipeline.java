package io.jenkins.blueocean.freestyle;

import java.util.Collection;

import io.jenkins.blueocean.rest.annotation.Capability;
import io.jenkins.blueocean.rest.hal.Link;
import io.jenkins.blueocean.rest.model.BlueActionProxy;
import io.jenkins.blueocean.rest.model.BlueFavorite;
import io.jenkins.blueocean.rest.model.BlueFavoriteAction;
import io.jenkins.blueocean.rest.model.BluePipeline;
import io.jenkins.blueocean.rest.model.BlueQueueContainer;
import io.jenkins.blueocean.rest.model.BlueRun;
import io.jenkins.blueocean.rest.model.BlueRunContainer;

@Capability("freestyle.project")
public class BlueFreestylePipeline extends BluePipeline {

    @Override
    public Link getLink() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public BlueFavorite favorite(BlueFavoriteAction arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Collection<BlueActionProxy> getActions() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getDisplayName() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Long getEstimatedDurationInMillis() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getFullName() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getJobClass() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getLastSuccessfulRun() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public BlueRun getLatestRun() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getName() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getOrganization() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public BlueQueueContainer getQueue() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public BlueRunContainer getRuns() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Integer getWeatherScore() {
        // TODO Auto-generated method stub
        return null;
    }

}
