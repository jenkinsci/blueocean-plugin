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
    public static final String WEATHER="weather";

    /**
     *  Branch name
     *
     *  @return gives branch name
     */
    @Exported(name = NAME)
    @JsonProperty(NAME)
    public abstract String getName();


    /**
     *  Branch name
     *
     *  @return gives branch name
     */
    @Exported(name = WEATHER)
    @JsonProperty(WEATHER)
    public abstract int getWeather();


    /**
     * @return Gives Runs in this pipeline
     */
    @JsonProperty(RUNS)
    public abstract BlueRunContainer getRuns();

}
