package io.jenkins.blueocean.rest.model;

import io.jenkins.blueocean.rest.Navigable;
import io.jenkins.blueocean.rest.annotation.Capability;
import org.kohsuke.stapler.export.Exported;

import javax.annotation.Nonnull;
import java.util.Collection;

import static io.jenkins.blueocean.rest.model.KnownCapabilities.BLUE_RUN;

/**
 * Common interface for job run details
 */
@Capability(BLUE_RUN)
public interface BlueItemRun {

    /**
     * @return name of the organization
     */
    @Exported(name = "organization")
    String getOrganization();

    /**
     * @return {@link BlueRun} id - unique within a pipeline
     */
    @Exported(name = "id")
    String getId();

    /**
     * @return Pipeline name - unique within an organization
     */
    @Exported(name = "pipeline")
    String getPipeline();

    @Exported(name = "name")
    String getName();

    @Exported(name = "description")
    String getDescription();

    /**
     * @return Gives change set of a run
     */
    @Exported(inline = true)
    @Nonnull
    @Navigable
    Container<BlueChangeSetEntry> getChangeSet();

    /**
     * @return run start time
     */
    @Exported(name = "startTime")
    String getStartTimeString();

    /**
     * Enque time
     */
    @Exported(name = "enQueueTime")
    String getEnQueueTimeString();

    /**
     * Run end time
     */
    @Exported(name = "endTime")
    String getEndTimeString();

    /**
     * @return Build duration in milli seconds
     */
    @Exported(name = "durationInMillis")
    Long getDurationInMillis();

    /**
     * @return Estimated Build duration in milli seconds
     */
    @Exported(name = "estimatedDurationInMillis")
    Long getEstimatedDurtionInMillis();

    /**
     * @return The state of the run
     */
    @Exported(name = "state")
    BlueRun.BlueRunState getStateObj();

    /**
     * @return The result state of the job (e.g unstable)
     */
    @Exported(name = "result")
    BlueRun.BlueRunResult getResult();

    /**
     * @return Build summary
     */
    @Exported(name = "runSummary")
    String getRunSummary();

    /**
     * @return Type of Run. Type name to be Jenkins Run.getClass().getSimpleName()
     */
    @Exported(name = "type")
    String getType();

    /**
     * @return Uri of artifacts zip file.
     */
    @Exported
    String getArtifactsZipFile();

    /**
     * @return Run artifacts
     */
    @Navigable
    BlueArtifactContainer getArtifacts();

    /**
     * @return Gives Actions associated with this Run, if requested via tree
     */
    @Navigable
    @Exported(name = "actions", inline = true)
    Collection<BlueActionProxy> getActions();

    /**
     * @return Gives tests in this run
     */
    @Navigable
    BlueTestResultContainer getTests();

    /**
     * @return Gives the test summary for this run
     */
    @Exported(name = "testSummary", inline = true, skipNull = true)
    BlueTestSummary getTestSummary();

    /**
     * @return Instance of stapler aware instance that can do the following:
     * <p></p><ul>
     * <li>Must be able to process start query parameter. 'start' parameter is the byte offset in the actual log file</li>
     * <li>Must produce following HTTP headers in the response</li>
     * <li>X-Text-Size  It is the byte offset of the raw log file client should use in the next request as value of start query parameter.</li>
     * <li>X-More-Data  If  its true, then client should repeat the request after some delay. In the repeated request it should use
     * X-TEXT-SIZE header value with *start* query parameter.</li>
     * </ul>
     */
    @Navigable
    Object getLog();

    /**
     * @return cause of the run being created
     */
    @Exported(name = "causes", inline = true)
    Collection<BlueRun.BlueCause> getCauses();

    /**
     * @return cause of what is blocking this run
     */
    @Exported(name = "causeOfBlockage")
    String getCauseOfBlockage();

    /**
     * @return if the run will allow a replay
     */
    @Exported(name = "replayable")
    boolean isReplayable();
}
