package io.jenkins.blueocean.rest.sandbox;

import org.kohsuke.stapler.export.Exported;

/**
 * API endpoint for an organization that houses all the pipelines.
 *
 * @author Kohsuke Kawaguchi
 */
public abstract class BOOrganization extends Resource {
    @Exported
    public abstract String getName();

    public abstract BOPipelineContainer getPipelines();

}

