package io.jenkins.blueocean.rest.sandbox;

import com.fasterxml.jackson.annotation.JsonProperty;
import hudson.model.Run;
import org.kohsuke.stapler.export.Exported;

import java.text.SimpleDateFormat;
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

    /** Date String format */
    public static final String DATE_FORMAT_STRING = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";

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

    /** Build execution start time inside executor */
    public abstract Date getStartTime();

    @JsonProperty("startTime")
    @Exported(name="startTime")
    public final String getStartTimeString(){
        return new SimpleDateFormat(DATE_FORMAT_STRING).format(getStartTime());
    }

    /**
     * Time when build is scheduled and is in queue waiting for executor
     */
    public abstract Date getEnQueueTime();

     @JsonProperty("enQueueTime")
     @Exported(name="enQueueTime")
     public final String getEnQueueTimeString() {
        return new SimpleDateFormat(DATE_FORMAT_STRING).format(getEnQueueTime());
     }

     /**Build end time*/
    public abstract Date getEndTime();

    @JsonProperty("endTime")
    @Exported(name="endTime")
    public final String getEndTimeString(){
        return new SimpleDateFormat(DATE_FORMAT_STRING).format(getEndTime());
    }

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

    public enum Status{
        /** Build completed successfully */
        SUCCESS,

        UNSTABLE,

        /** Build failed */
        FAILURE,

        /** In multi stage build (maven2), a build step might not execute due to failure in previous step */
        NOT_BUILT,

        /** Unknown status */
        UNKNOWN;

        static Status get(String value){
            if(value.equals(SUCCESS)){
                return SUCCESS;
            }else if(value.equals(UNSTABLE)){
                return UNSTABLE;
            }else if(value.equals(FAILURE)){
                return FAILURE;
            }else if(value.equals(NOT_BUILT)){
                return NOT_BUILT;
            }else{
                return UNKNOWN;
            }
        }
    }

}
