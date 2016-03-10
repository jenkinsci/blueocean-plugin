package io.jenkins.blueocean.rest.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.kohsuke.stapler.export.Exported;

/**
 * Pipeline Branch API
 *
 * @author Vivek Pandey
 */
public abstract class BlueBranch extends Resource{
    public static final String NAME="name";
    public static final String RUNS="runs";
    public static final String WEATHER_SCORE ="weatherScore";
    public static final String LATEST_RUN = "latestRun";

    /**
     *  Branch name
     *
     *  @return gives branch name
     */
    @Exported(name = NAME)
    @JsonProperty(NAME)
    public abstract String getName();


    /**
     *  Weather score
     *
     *  @return gives weather score
     */
    @Exported(name = WEATHER_SCORE)
    @JsonProperty(WEATHER_SCORE)
    public abstract int getWeatherScore();

    /**
     * @return The Latest Run for the branch
     */
    @Exported(name = LATEST_RUN, inline = true)
    @JsonProperty(LATEST_RUN)
    public abstract BlueRun getLatestRun();

    /**
     * @return Gives Runs in this pipeline
     */
    @JsonProperty(RUNS)
    public abstract BlueRunContainer getRuns();
}
