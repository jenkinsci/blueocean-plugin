package io.jenkins.blueocean.rest.model;

import io.jenkins.blueocean.rest.Navigable;
import io.jenkins.blueocean.rest.annotation.Capability;
import org.kohsuke.stapler.WebMethod;
import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.json.JsonBody;
import org.kohsuke.stapler.verb.PUT;

import java.util.Collection;

/**
 * Defines pipeline state and its routing
 *
 * @author Vivek Pandey
 */
@Capability("io.jenkins.blueocean.rest.model.BluePipeline")
public abstract class BluePipeline extends Resource {
    public static final String ORGANIZATION="organization";
    public static final String NAME="name";
    public static final String DISPLAY_NAME="displayName";
    public static final String FULL_NAME="fullName";
    public static final String WEATHER_SCORE ="weatherScore";
    public static final String LATEST_RUN = "latestRun";
    public static final String ESTIMATED_DURATION = "estimatedDurationInMillis";
    public static final String LAST_SUCCESSFUL_RUN = "lastSuccessfulRun";
    public static final String ACTIONS = "actions";

    /**
     * @return name of the organization
     */
    @Exported(name = ORGANIZATION)
    public abstract String getOrganization();

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
     * @return Includes parentLink folders if any. For example folder1/folder2/p1
     */
    @Exported(name = FULL_NAME)
    public abstract String getFullName();

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

    @Exported(name= LAST_SUCCESSFUL_RUN)
    public abstract String getLastSuccessfulRun();


    /**
     * @return Estiamated duration based on last pipeline runs. -1 is returned if there is no estimate available.
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

    @PUT
    @WebMethod(name="favorite")
    public abstract void favorite(@JsonBody FavoriteAction favoriteAction);

    public static class FavoriteAction {
        private boolean favorite;

        public void setFavorite(boolean favorite) {
            this.favorite = favorite;
        }

        public boolean isFavorite() {
            return favorite;
        }
    }
}
