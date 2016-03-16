package io.jenkins.blueocean.rest.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.jenkins.blueocean.rest.Navigable;
import org.kohsuke.stapler.export.Exported;

/**
 * Defines pipeline state and its routing
 *
 * @author Vivek Pandey
 */
public abstract class BluePipeline extends Resource {
    public static final String ORGANIZATION="organization";
    public static final String NAME="name";
    public static final String DISPLAY_NAME="displayName";
    public static final String BRANCHES="branches";
    public static final String RUNS="runs";
    public static final String WEATHER_SCORE ="weatherScore";

    /**
     * @return name of the organization
     */
    @Exported(name = ORGANIZATION)
    @JsonProperty(ORGANIZATION)
    public abstract String getOrganization();

    /**
     * @return name of the pipeline
     */
    @Exported(name = NAME)
    @JsonProperty(NAME)
    public abstract String getName();

    /**
     * @return human readable name of this pipeline
     */
    @Exported(name = DISPLAY_NAME)
    @JsonProperty(DISPLAY_NAME)
    public abstract String getDisplayName();


    /**
     * @return weather health score percentile
     */
    @Exported(name = WEATHER_SCORE)
    @JsonProperty(WEATHER_SCORE)
    public abstract int getWeatherScore();

    /**
     * @return Gives Runs in this pipeline
     */
    @JsonProperty(RUNS)
    @Navigable
    public abstract BlueRunContainer getRuns();

}
