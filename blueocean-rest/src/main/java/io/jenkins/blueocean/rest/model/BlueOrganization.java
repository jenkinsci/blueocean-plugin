package io.jenkins.blueocean.rest.model;

import io.jenkins.blueocean.rest.Navigable;
import io.jenkins.blueocean.rest.annotation.Capability;
import org.kohsuke.stapler.export.Exported;

import static io.jenkins.blueocean.rest.model.KnownCapabilities.BLUE_ORGANIZATION;

/**
 * API endpoint for an organization that houses all the pipelines.
 *
 * @author Kohsuke Kawaguchi
 */
@Capability(BLUE_ORGANIZATION)
public abstract class BlueOrganization extends Resource {
    public static final String NAME="name";
    public static final String DISPLAY_NAME="name";
    public static final String PIPELINES="pipelines";

    @Exported(name = NAME)
    public abstract String getName();

    @Exported(name = DISPLAY_NAME)
    public abstract String getDisplayName();

    @Navigable
    //   /organizations/jenkins/piplelines/f1
    public abstract BluePipelineContainer getPipelines();

    /**
     * A set of users who belong to this organization.
     *
     * @return {@link BlueUserContainer}
     */
    @Navigable
    public abstract BlueUserContainer getUsers();

    /**
     *  Gives currently authenticated user
     *
     * @return {@link BlueUser}
     */
    @Navigable
    public abstract BlueUser getUser();
}

