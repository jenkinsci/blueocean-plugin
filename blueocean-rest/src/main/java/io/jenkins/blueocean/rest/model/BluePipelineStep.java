package io.jenkins.blueocean.rest.model;

import io.jenkins.blueocean.rest.Navigable;
import io.jenkins.blueocean.rest.annotation.Capability;
import java.util.Collection;
import java.util.Date;
import org.kohsuke.stapler.HttpResponse;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.WebMethod;
import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.verb.POST;
import static io.jenkins.blueocean.rest.model.BlueRun.STATE;
import static io.jenkins.blueocean.rest.model.KnownCapabilities.BLUE_PIPELINE_STEP;

/**
 * Pipeline Step resource
 *
 * @author Vivek Pandey
 */
@Capability(BLUE_PIPELINE_STEP)
public abstract class BluePipelineStep extends Resource{
    public static final String DISPLAY_NAME="displayName";
    public static final String DISPLAY_DESCRIPTION="displayDescription";
    public static final String RESULT = "result";
    public static final String START_TIME="startTime";
    public static final String ID = "id";
    public static final String EDGES = "edges";
    public static final String DURATION_IN_MILLIS="durationInMillis";
    public static final String ACTIONS = "actions";
    public static final String TYPE = "type";

    /**
     * id of step.
     * @return node id
     */
    @Exported(name = ID)
    public abstract String getId();

    /**
     * Step display name.
     * @return display name
     */
    @Exported(name = DISPLAY_NAME)
    public abstract String getDisplayName();

    /**
     * Step display description.
     * @return display description
     */
    @Exported(name = DISPLAY_DESCRIPTION)
    public abstract String getDisplayDescription();

    /**
     * Type of step.
     * @return step type
     */
    @Exported(name = TYPE)
    public abstract String getType();

    /**
     * Step execution result
     * @return {@link io.jenkins.blueocean.rest.model.BlueRun.BlueRunResult} instance
     */
    @Exported(name = RESULT)
    public abstract BlueRun.BlueRunResult getResult();

    /**
     * Step execution state
     * @return execution state {@link io.jenkins.blueocean.rest.model.BlueRun.BlueRunState}
     */
    @Exported(name=STATE)
    public abstract BlueRun.BlueRunState getStateObj();

    /**
     * Start time of execution
     * @return start time of execution
     */
    public abstract Date getStartTime();

    /**
     * Start time string representation
     * @return start time of execution
     */
    @Exported(name = START_TIME)
    public abstract String getStartTimeString();

    /**
     * Execution duration
     * @return execution duration in milli seconds
     */
    @Exported(name= DURATION_IN_MILLIS)
    public abstract Long getDurationInMillis();

    /**
     * @return Gives logs associated with this node
     */
    public abstract Object getLog();

    /**
     *
     * @return Gives Actions associated with this pipeline node
     */
    @Navigable
    @Exported(name = ACTIONS, inline = true)
    public abstract Collection<BlueActionProxy> getActions();

    /**
     * Input step associated with this step
     * @return input step
     */
    @Exported(name="input", inline = true)
    public abstract BlueInputStep getInputStep();

    /**
     * Processes submitted input step via POST request
     * @param request stapler request
     * @return http response
     */
    @POST
    @WebMethod(name = "")
    public abstract HttpResponse submitInputStep(StaplerRequest request);

}
