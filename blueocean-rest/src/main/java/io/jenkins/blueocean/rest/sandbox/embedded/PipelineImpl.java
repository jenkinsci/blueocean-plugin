package io.jenkins.blueocean.rest.sandbox.embedded;

/**
 * @author Kohsuke Kawaguchi
 */
public class PipelineImpl {
    private final OrganizationImpl parent;

    protected PipelineImpl(OrganizationImpl parent) {
        this.parent = parent;
    }


}
