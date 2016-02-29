package io.jenkins.blueocean.service.embedded.rest;

import hudson.model.Run;

/**
 * Pipeline Run
 *
 * @author Vivek Pandey
 */
public class PipelineRunImpl extends AbstractRunImpl {
    public PipelineRunImpl(Run run) {
        super(run);
    }
}
