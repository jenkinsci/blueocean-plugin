package io.jenkins.blueocean.listeners;

import hudson.Extension;
import hudson.model.Cause;
import hudson.model.CauseAction;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.model.listeners.RunListener;
import io.jenkins.blueocean.service.embedded.DownstreamJobAction;

/**
 * Listens to creation of jobs that are triggered by an upstream job, much like BuildTriggerListener from
 * pipeline-build-step. Unlike BuildTriggerListener, here we just add an action to the upstream job for later retrieval
 * rather than injecting information into its build log, in order to be a little more flexible at the cost of a bit more
 * work later to retrieve the details.
 */
@Extension
public class DownstreamJobListener extends RunListener<Run<?, ?>> {

    @Override
    public void onStarted(Run<?, ?> run, TaskListener listener) {
        for (CauseAction action : run.getActions(CauseAction.class)) {
            for (Cause cause : action.getCauses()) {
                if (cause instanceof Cause.UpstreamCause) {
                    Run triggerRun = ((Cause.UpstreamCause) cause).getUpstreamRun();
                    if (triggerRun != null) {
                        // Add a link from the upstream job to this newly spawned one, to be retrieved later
                        triggerRun.addAction(new DownstreamJobAction(run));
                    }
                }
            }
        }
    }
}
