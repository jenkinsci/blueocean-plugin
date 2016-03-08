package io.jenkins.blueocean.rest.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
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
@JsonInclude
public abstract class BlueRun extends Resource {
    public static final String ORGANIZATION="organization";
    public static final String ID="id";
    public static final String PIPELINE="pipeline";
    public static final String START_TIME="startTime";
    public static final String END_TIME="endTime";
    public static final String ENQUEUE_TIME="enQueueTime";
    public static final String DURATION_IN_MILLIS="durationInMillis";
    public static final String BRANCH = "branch";
    public static final String COMMIT_ID = "commitId";
    public static final String TYPE = "type";
    public static final String RUN_SUMMARY = "runSummary";
    public static final String RESULT = "result";
    public static final String STATE = "state";


    /** Date String format */
    public static final String DATE_FORMAT_STRING = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";

    /**
     * @return name of the organization
     */
    @Exported(name = ORGANIZATION)
    @JsonProperty(ORGANIZATION)
    public abstract String getOrganization();

    /**
     * @return {@link BlueRun} id - unique within a pipeline
     */
    @Exported(name = ID)
    @JsonProperty(ID)
    public abstract String getId();

    /**
     * @return Pipeline name - unique within an organization
     */
    @Exported(name = PIPELINE)
    @JsonProperty(PIPELINE)
    public abstract String getPipeline();


    /**
     * @return Build execution start time inside executor
     */
    public abstract Date getStartTime();

    /**
     * @return Gives change set of a run
     */
    @Exported(inline = true)
    public abstract Container<?> getChangeSet();

    /**
     * @return run start time
     */
    @JsonProperty(START_TIME)
    @Exported(name=START_TIME)
    public final String getStartTimeString(){
        return new SimpleDateFormat(DATE_FORMAT_STRING).format(getStartTime());
    }

    /**
     * @return Time when build is scheduled and is in queue waiting for executor
     */
    public abstract Date getEnQueueTime();

     @JsonProperty(ENQUEUE_TIME)
     @Exported(name=ENQUEUE_TIME)
     public final String getEnQueueTimeString() {
        return new SimpleDateFormat(DATE_FORMAT_STRING).format(getEnQueueTime());
     }

    /**
     * @return Build end time
     */
    public abstract Date getEndTime();

    @JsonProperty(END_TIME)
    @Exported(name=END_TIME)
    public final String getEndTimeString(){
        return new SimpleDateFormat(DATE_FORMAT_STRING).format(getEndTime());
    }

    /**
     * @return Build duration in milli seconds
     */
    @JsonProperty(DURATION_IN_MILLIS)
    @Exported(name = DURATION_IN_MILLIS)
    public abstract Long getDurationInMillis();

    /**
     *
     * @return The state of the run
     */
    @Exported(name=STATE)
    @JsonProperty(STATE)
    public abstract BlueRunState getStateObj();

    /**
     *
     * @return The result state of the job (e.g unstable)
     */
    @Exported(name= RESULT)
    @JsonProperty(RESULT)
    public abstract BlueRunResult getResult();

    /**
     * @return Build summary
     */
    @JsonProperty(RUN_SUMMARY)
    @Exported(name = RUN_SUMMARY)
    public abstract String getRunSummary();

    /**
     * @return Type of Run. Type name to be Jenkins Run.getClass().getSimpleName()
     */
    @JsonProperty(TYPE)
    @Exported(name=TYPE)
    public abstract String getType();

    /**
     * @return Instance of stapler aware instance that can do the following:
     * <p></p><ul>
     *  <li>Must be able to process start query parameter. 'start' parameter is the byte offset in the actual log file</li>
     *  <li>Must produce following HTTP headers in the response</li>
     *  <li>X-Text-Size  It is the byte offset of the raw log file client should use in the next request as value of start query parameter.</li>
     *  <li>X-More-Data  If  its true, then client should repeat the request after some delay. In the repeated request it should use
     *                    X-TEXT-SIZE header value with *start* query parameter.</li>
     *  </ul>
     */
    public abstract Object getLog();

    public enum BlueRunState {
        NOT_STARTED,
        RUNNING,
        FINISHED
    }

    public enum BlueRunResult {
        /** Build completed successfully */
        SUCCESS,

        UNSTABLE,

        /** Build failed */
        FAILURE,

        /** In multi stage build (maven2), a build step might not execute due to failure in previous step */
        NOT_BUILT,

        /** Unknown status */
        UNKNOWN;
    }
}
