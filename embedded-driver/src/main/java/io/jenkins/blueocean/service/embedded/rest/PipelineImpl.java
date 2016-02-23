package io.jenkins.blueocean.service.embedded.rest;

/**
 * @author Kohsuke Kawaguchi
 */
public class PipelineImpl {
    private final OrganizationImpl parent;

    protected PipelineImpl(OrganizationImpl parent) {
        this.parent = parent;
    }


}
