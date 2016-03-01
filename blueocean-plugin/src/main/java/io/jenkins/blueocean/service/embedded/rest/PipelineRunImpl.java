package io.jenkins.blueocean.service.embedded.rest;

import hudson.model.Run;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;

/**
 * Pipeline Run
 *
 * @author Vivek Pandey
 */
public class PipelineRunImpl extends AbstractRunImpl {
    private final WorkflowRun run;
    public PipelineRunImpl(WorkflowRun run) {
        this.run = run;
    }

    @Override
    protected Run getRun() {
        return run;
    }

    @Override
    public String getBranch() {
        return run.getParent().getName();
    }

    @Override
    public String getCommitId() {
        return null;
    }
}
