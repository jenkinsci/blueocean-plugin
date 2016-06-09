package io.jenkins.blueocean.rest.model;

import io.jenkins.blueocean.rest.Navigable;
import org.kohsuke.stapler.export.Exported;

/**
 * API endpoint for an organization that houses all the pipelines.
 *
 * @author Kohsuke Kawaguchi
 */
public abstract class BlueOrganization extends Resource {
    public static final String NAME="name";
    public static final String PIPELINES="pipelines";

    @Exported(name = NAME)
    public abstract String getName();

    @Navigable
    public abstract BluePipelineContainer getPipelines();

    /**
     * A set of users who belong to this organization.
     *
     * @return {@link BlueUserContainer}
     */
    @Navigable
    public abstract BlueUserContainer getUsers();

    @Override
    public String getUrlName() {
        return getName();
    }
}

