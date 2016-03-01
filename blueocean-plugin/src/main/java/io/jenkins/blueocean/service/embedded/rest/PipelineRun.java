package io.jenkins.blueocean.service.embedded.rest;

import hudson.model.Run;

/**
 * Pipeline Run
 *
 * @author Vivek Pandey
 */
public class PipelineRun extends AbstractRunImpl {
    public PipelineRun(Run run) {
        super(run);
    }
}
