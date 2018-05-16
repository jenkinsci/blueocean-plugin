package io.jenkins.blueocean.rest.model;

import io.jenkins.blueocean.rest.Navigable;
import org.kohsuke.stapler.export.Exported;

import java.util.List;

public interface BlueRunnableItem /* extends BluePipelineItem */ {
    /**
     * @return weather health score percentile
     */
    @Exported(name = BluePipeline.WEATHER_SCORE)
    Integer getWeatherScore();

    /**
     * @return The Latest Run for the branch
     */
    @Exported(name = BluePipeline.LATEST_RUN, inline = true)
    BlueRun getLatestRun();

    /**
     * @return Estimated duration based on last pipeline runs. -1 is returned if there is no estimate available.
     *
     */
    @Exported(name = BluePipeline.ESTIMATED_DURATION)
    Long getEstimatedDurationInMillis();

    /**
     * @return Gives Runs in this pipeline
     */
    @Navigable
    BlueRunContainer getRuns();

    /**
     * @return Gives {@link BlueQueueContainer}
     */
    @Navigable
    BlueQueueContainer getQueue();

    /**
     * List of build parameters
     */
    @Exported(name = "parameters", inline = true)
    List<Object> getParameters();

    /**
     * @return trend data related to this pipeline
     */
    @Navigable
    BlueTrendContainer getTrends();
}
