package io.jenkins.blueocean.rest.model;

import io.jenkins.blueocean.commons.stapler.TreeResponse;
import io.jenkins.blueocean.rest.Navigable;
import io.jenkins.blueocean.rest.annotation.Capability;
import org.kohsuke.stapler.WebMethod;
import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.json.JsonBody;
import org.kohsuke.stapler.verb.PUT;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static io.jenkins.blueocean.rest.model.KnownCapabilities.BLUE_PIPELINE;

/**
 * Defines pipeline state and its routing
 *
 * @author Vivek Pandey
 */
@Capability(BLUE_PIPELINE)
public abstract class BluePipeline extends Resource {
    public static final String ORGANIZATION="organization";
    public static final String NAME="name";
    public static final String DISPLAY_NAME="displayName";
    public static final String FULL_NAME="fullName";
    public static final String FULL_DISPLAY_NAME="fullDisplayName";
    public static final String WEATHER_SCORE ="weatherScore";
    public static final String LATEST_RUN = "latestRun";
    public static final String ESTIMATED_DURATION = "estimatedDurationInMillis";
    public static final String ACTIONS = "actions";
    public static final String PERMISSIONS= "permissions";

    /** Create pipeline */
    public static final String CREATE_PERMISSION = "create";

    /** Read pipeline permission */
    public static final String READ_PERMISSION = "read";

    /** start pipeline run */
    public static final String START_PERMISSION = "start";

    /** stop pipeline run */
    public static final String STOP_PERMISSION = "stop";

    /** configure pipeline permission */
    public static final String CONFIGURE_PERMISSION = "configure";

    /** build parameters */
    private static final String PARAMETERS = "parameters";

    /**
     * @return the organization that this pipeline is a child of
     */
    @Nonnull
    public abstract BlueOrganization getOrganization();

    /**
     * @return name of the organization
     */
    @Exported(name = ORGANIZATION)
    public abstract String getOrganizationName();

    /**
     * @return name of the pipeline
     */
    @Exported(name = NAME)
    public abstract String getName();

    /**
     * @return human readable name of this pipeline
     */
    @Exported(name = DISPLAY_NAME)
    public abstract String getDisplayName();

    /**
     * @return Includes parent folders names if any. For example folder1/folder2/p1
     */
    @Exported(name = FULL_NAME)
    public abstract String getFullName();


    /**
     * @return Includes display names of parent folders if any. For example folder1/myFolder2/p1
     */
    @Exported(name = FULL_DISPLAY_NAME)
    public abstract String getFullDisplayName();


    /**
     * @return weather health score percentile
     */
    @Exported(name = WEATHER_SCORE)
    public abstract Integer getWeatherScore();

    /**
     * @return The Latest Run for the branch
     */
    @Exported(name = LATEST_RUN, inline = true)
    public abstract BlueRun getLatestRun();

    /**
     * @return Estimated duration based on last pipeline runs. -1 is returned if there is no estimate available.
     *
     */
    @Exported(name = ESTIMATED_DURATION)
    public abstract Long getEstimatedDurationInMillis();

    /**
     * @return Gives Runs in this pipeline
     */
    @Navigable
    public abstract BlueRunContainer getRuns();


    /**
     *
     * @return Gives Actions associated with this Run
     */
    @Navigable
    @Exported(name = ACTIONS, inline = true)
    public abstract Collection<BlueActionProxy> getActions();

    /**
     * @return Gives {@link BlueQueueContainer}
     */
    @Navigable
    public abstract BlueQueueContainer getQueue();

    /**
     * List of build parameters
     */
    @Exported(name = PARAMETERS, inline = true)
    public abstract List<Object> getParameters();

    @PUT
    @WebMethod(name="favorite")
    @TreeResponse
    public abstract BlueFavorite favorite(@JsonBody BlueFavoriteAction favoriteAction);


    /**
     * Gives permissions of user in context for a given pipeline.
     *
     * Following permissions are returned as key to the permission map: create, start, stop, read for a pipeline job:
     *
     * <p>
     * create: User can create a pipeline
     * <p>
     * start: User can start a run of this pipeline. If not applicable to certain pipeline then can be false or null.
     * <p>
     * stop: User can stop a run of this pipeline. If not applicable to certain pipeline then can be false or null.
     * <p>
     * read: User has permission to view this pipeline
     *
     * <p>
     * For example for anonymous user with security enabled and only read permission, the permission map for a pipeline job is:
     *
     * <pre>
     * "permissions":{
     *     "start": false,
     *     "stop": false,
     *     "create":false,
     *     "read": true
     * }
     * </pre>
     *
     * Implementation of BluePipeline can provide their own set of permissions in addition to the ones defined
     *
     * @return permission map
     */
    @Exported(name = PERMISSIONS)
    public abstract Map<String, Boolean> getPermissions();

    /**
     * @return Gives scm resource attached to this pipeline
     */
    public abstract BluePipelineScm getScm();

    /**
     * @return trend data related to this pipeline
     */
    @Navigable
    public abstract BlueTrendContainer getTrends();
}
