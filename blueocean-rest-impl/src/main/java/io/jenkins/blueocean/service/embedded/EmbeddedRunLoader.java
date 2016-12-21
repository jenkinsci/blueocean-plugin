package io.jenkins.blueocean.service.embedded;

import hudson.model.Job;
import hudson.model.Run;
import io.jenkins.blueocean.rest.Reachable;
import io.jenkins.blueocean.rest.RunLoader;
import io.jenkins.blueocean.rest.model.BlueRun;
import io.jenkins.blueocean.service.embedded.rest.AbstractRunImpl;

public abstract class EmbeddedRunLoader extends RunLoader {
    @Override
    public BlueRun getRun(String id, Job job, Reachable parent) {
        Run run = job.getBuild(id);
        return AbstractRunImpl.getBlueRun(run, parent);
    }
}
