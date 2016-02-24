package io.jenkins.blueocean.rest.sandbox;

import org.kohsuke.stapler.export.Exported;

/**
 * API endpoint for an organization that houses all the pipelines.
 *
 * @author Kohsuke Kawaguchi
 */
public abstract class Organization extends Resource {
    @Exported
    public abstract String getName();

    public abstract PipelineContainer getPipelines();

}

