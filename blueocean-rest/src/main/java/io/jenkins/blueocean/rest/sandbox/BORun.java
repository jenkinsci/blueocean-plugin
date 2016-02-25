package io.jenkins.blueocean.rest.sandbox;

import com.fasterxml.jackson.annotation.JsonProperty;
import hudson.model.Run;
import org.kohsuke.stapler.export.Exported;

import java.util.Date;

/**
 * BlueOCean Run model.
 *
 * Implementers of different Run type {@link #getType()} can add additional data to this model. e.g. A FreeStyle job can
 * expose change log or Pipeline run can add steps and their status etc.
 *
 * @author Vivek Pandey
 */
public abstract class BORun extends Resource {
    /** Name of the organization */
    @Exported
    @JsonProperty("organization")
    public abstract String getOrganization();

    /** BORun id - unique within a pipeline */
    @JsonProperty("id")
    @Exported
    public abstract String getId();

    /** Pipeline name - unique within an organization */
    @JsonProperty("pipeline")
    @Exported
    public abstract String getPipeline();

    /** BORun status */
    @JsonProperty("status")
    @Exported
    public abstract Status getStatus();

    /** BORun trend */
    @JsonProperty("runTrend")
    @Exported
    public abstract RunTrend getRunTrend();


    /** Build execution start time inside executor */
    @JsonProperty("startTime")
    @Exported
    public abstract Date getStartTime();

    /** Time when build is scheduled and is in queue waiting for executor */
    @JsonProperty("enQueueTime")
    @Exported
    public abstract Date getEnQueueTime();

    /**Build end time*/
    @JsonProperty("endTime")
    @Exported
    public abstract Date getEndTime();

    /**  Build duration in milli seconds */
    @JsonProperty("durationInMillis")
    @Exported
    public abstract Long getDurationInMillis();

    /**  Branch on which build is executed */
    @JsonProperty("branch")
    @Exported
    public abstract String getBranch();

    /** Commit id on which build is executing */
    @JsonProperty("commitId")
    @Exported
    public abstract String getCommitId();

    /** Build summary */
    @JsonProperty("runSummary")
    @Exported
    public abstract String getRunSummary();

    /** Result of run */
    @JsonProperty("result")
    @Exported

    /** Type of Run. Type name to be Jenkins {@link Run#getClass()#getSimpleName()} */
    public abstract String getType();

    public enum Status {
        /** Build completed successfully */
        SUCCESSFUL,

        /** Build failed */
        FAILING,

        /** Build is executing, not in the queue */
        EXECUTING,

        /** Build is in queue, waiting to be executed */
        IN_QUEUE,

        /** Build was aborted by user */
        ABORTED,

        /** Unknown status */
        UNKNOWN,

        /** In multi stage build (maven2), a build step might not execute due to failure in previous step */
        NOT_BUILT
    }

    public enum RunTrend {
        /** Build was broekn in earlier build, got fixed now */
        FIXED,

        /** Build has been broken for a while */
        BROKEN_FOR_LONG_TIME
    }

}
