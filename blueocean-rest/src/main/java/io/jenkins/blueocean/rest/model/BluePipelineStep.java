package io.jenkins.blueocean.rest.model;

import io.jenkins.blueocean.rest.Navigable;
import io.jenkins.blueocean.rest.annotation.Capability;
import org.kohsuke.stapler.HttpResponse;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.WebMethod;
import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.verb.POST;

import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;

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

    @Exported(name = ID)
    public abstract String getId();

    @Exported(name = DISPLAY_NAME)
    public abstract String getDisplayName();

    @Exported(name = DISPLAY_DESCRIPTION)
    public abstract String getDisplayDescription();

    @Exported(name = RESULT)
    public abstract BlueRun.BlueRunResult getResult();

    @Exported(name=STATE)
    public abstract BlueRun.BlueRunState getStateObj();

    public abstract Date getStartTime();

    @Exported(name = START_TIME)
    public final String getStartTimeString(){
        if(getStartTime() == null) {
            return null;
        }
        return new SimpleDateFormat(BlueRun.DATE_FORMAT_STRING).format(getStartTime());
    }

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

    @Exported(name="input", inline = true)
    public abstract BlueInputStep getInputStep();

    @POST
    @WebMethod(name = "")
    public abstract HttpResponse submitInputStep(StaplerRequest request);

}
