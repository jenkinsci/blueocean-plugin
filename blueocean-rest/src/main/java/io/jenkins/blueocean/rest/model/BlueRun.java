package io.jenkins.blueocean.rest.model;

import io.jenkins.blueocean.commons.stapler.TreeResponse;
import io.jenkins.blueocean.rest.Navigable;
import io.jenkins.blueocean.rest.annotation.Capability;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.WebMethod;
import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.verb.POST;
import org.kohsuke.stapler.verb.PUT;

import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;

import static io.jenkins.blueocean.rest.model.KnownCapabilities.BLUE_RUN;

/**
 * BlueOCean Run model.
 *
 * Implementers of different Run type {@link #getType()} can add additional data to this model. e.g. A FreeStyle job can
 * expose change log or Pipeline run can add steps and their status etc.
 *
 * @author Vivek Pandey
 */
@Capability(BLUE_RUN)
public abstract class BlueRun extends Resource {
    public static final String ORGANIZATION="organization";
    public static final String ID="id";
    public static final String PIPELINE="pipeline";
    public static final String START_TIME="startTime";
    public static final String END_TIME="endTime";
    public static final String ENQUEUE_TIME="enQueueTime";
    public static final String DURATION_IN_MILLIS="durationInMillis";
    public static final String ESTIMATED_DURATION_IN_MILLIS="estimatedDurationInMillis";
    public static final String TYPE = "type";
    public static final String RUN_SUMMARY = "runSummary";
    public static final String RESULT = "result";
    public static final String STATE = "state";
    public static final String CAUSE_OF_BLOCKAGE = "causeOfBlockage";
    public static final String ACTIONS = "actions";
    public static final String TEST_SUMMARY = "testSummary";

    public static final int DEFAULT_BLOCKING_STOP_TIMEOUT_IN_SECS=10;



    /** Date String format */
    public static final String DATE_FORMAT_STRING = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";


    /**
     * @return name of the organization
     */
    @Exported(name = ORGANIZATION)
    public abstract String getOrganization();

    /**
     * @return {@link BlueRun} id - unique within a pipeline
     */
    @Exported(name = ID)
    public abstract String getId();

    /**
     * @return Pipeline name - unique within an organization
     */
    @Exported(name = PIPELINE)
    public abstract String getPipeline();


    /**
     * @return Build execution start time inside executor
     */
    public abstract Date getStartTime();

    /**
     * @return Gives change set of a run
     */
    @Exported(inline = true)
    public abstract Container<BlueChangeSetEntry> getChangeSet();

    /**
     * @return run start time
     */
    @Exported(name=START_TIME)
    public final String getStartTimeString(){
        return new SimpleDateFormat(DATE_FORMAT_STRING).format(getStartTime());
    }

    /**
     * @return Time when build is scheduled and is in queue waiting for executor
     */
    public abstract Date getEnQueueTime();

     @Exported(name=ENQUEUE_TIME)
     public final String getEnQueueTimeString() {
        return new SimpleDateFormat(DATE_FORMAT_STRING).format(getEnQueueTime());
     }

    /**
     * @return Build end time
     */
    public abstract Date getEndTime();

    @Exported(name=END_TIME)
    public final String getEndTimeString(){
        Date endTime = getEndTime();
        if(endTime == null) {
            return null;
        } else {
            return new SimpleDateFormat(DATE_FORMAT_STRING).format(endTime);
        }
    }

    /**
     * @return Build duration in milli seconds
     */
    @Exported(name = DURATION_IN_MILLIS)
    public abstract Long getDurationInMillis();

    /**
     * @return Estimated Build duration in milli seconds
     */
    @Exported(name = ESTIMATED_DURATION_IN_MILLIS)
    public abstract Long getEstimatedDurtionInMillis();

    /**
     *
     * @return The state of the run
     */
    @Exported(name=STATE)
    public abstract BlueRunState getStateObj();

    /**
     *
     * @return The result state of the job (e.g unstable)
     */
    @Exported(name= RESULT)
    public abstract BlueRunResult getResult();

    /**
     * @return Build summary
     */
    @Exported(name = RUN_SUMMARY)
    public abstract String getRunSummary();

    /**
     * @return Type of Run. Type name to be Jenkins Run.getClass().getSimpleName()
     */
    @Exported(name=TYPE)
    public abstract String getType();

    /**
     * Attempt to stop ongoing run.
     *
     * @param blocking if true then tries to stop till times out.
     *
     * @param timeOutInSecs if blocking is true then defines timeout value in seconds. Default 10 sec. If non-blocking
     *                      then this parameter is ignored.
     *
     * @return Blue run instance. Caller should look at state field to determine if stop was successful. If state is
     *         {@link BlueRun.BlueRunState#FINISHED} then stop was successful or run finished normally.
     *
     */
    @PUT
    @TreeResponse
    @WebMethod(name="stop")
    public abstract BlueRun stop(@QueryParameter("blocking") Boolean blocking, @QueryParameter("timeOutInSecs") Integer timeOutInSecs);

    /**
     * @return Uri of artifacts zip file.
     */
    @Exported
    public abstract String getArtifactsZipFile();
    /**
     *
     * @return Run artifacts
     */
    @Navigable
    public abstract BlueArtifactContainer getArtifacts();

    /**
     * @return Serves .../runs/{rundId}/nodes/ and provides pipeline execution nodes
     * @see BluePipelineNode
     */
    public abstract BluePipelineNodeContainer getNodes();

    /**
     *
     * @return Gives Actions associated with this Run
     */
    @Navigable
    @Exported(name = ACTIONS, inline = true)
    public abstract Collection<BlueActionProxy> getActions();

    /**
     * @return Gives steps from pipeline. The list of steps must not include stages, this is because stage could be
     * interpreted as step as its StepAtomNode and implementation of this API must ensure not to include it.
     */
    public abstract BluePipelineStepContainer getSteps();

    /**
     * @return Gives tests in this run
     */
    @Navigable
    public abstract BlueTestResultContainer getTests();

    /**
     * @return Gives the test summary for this run
     */
    @Exported(name = TEST_SUMMARY, inline = true, skipNull = true)
    public abstract BlueTestSummary getTestSummary();

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
    @Navigable
    public abstract Object getLog();

    /**
     * Replays a pipeline. The SCM commit/revision used in the existing and new runs should match.
     *
     * @return The queued item.
     */
    @POST @TreeResponse @WebMethod(name = "replay")
    public abstract BlueRun replay();

    @Exported(name = CAUSE_OF_BLOCKAGE)
    public abstract String getCauseOfBlockage();

    public enum BlueRunState {
        QUEUED,
        RUNNING,
        PAUSED,
        SKIPPED,
        NOT_BUILT,
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
        UNKNOWN,

        /** Aborted run*/
        ABORTED,
    }
}
