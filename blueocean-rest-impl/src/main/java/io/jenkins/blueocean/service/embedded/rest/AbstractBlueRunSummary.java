package io.jenkins.blueocean.service.embedded.rest;

import hudson.model.Run;
import io.jenkins.blueocean.rest.Reachable;
import io.jenkins.blueocean.rest.model.BlueActionProxy;
import io.jenkins.blueocean.rest.model.BlueOrganization;
import io.jenkins.blueocean.rest.model.BlueRun;
import io.jenkins.blueocean.rest.model.BlueTestSummary;
import java.util.Collection;
import java.util.Collections;

/**
 * Abstract BlueOcean run summary. Implementations must provide Jenkins build specific capabilities for UI to render
 * run details properly. Implementations can also add more properties to the summary.
 *
 * @author Vivek Pandey
 */
public abstract class AbstractBlueRunSummary extends AbstractRunImpl {
    protected final BlueRun blueRun;
    public AbstractBlueRunSummary(BlueRun blueRun, Run run, Reachable parent, BlueOrganization organization) {
        super(run, parent, organization);
        this.blueRun = blueRun;
    }

    @Override
    public Collection<BlueActionProxy> getActions() {
        return Collections.emptyList();
    }

    @Override
    public BlueTestSummary getTestSummary() {
        return null;
    }

    @Override
    public String getCauseOfBlockage() {
        return blueRun.getCauseOfBlockage();
    }

    @Override
    public BlueRunState getStateObj() {
        return blueRun.getStateObj();
    }

    @Override
    public boolean isReplayable() {
        return blueRun.isReplayable();
    }
}
