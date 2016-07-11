package io.jenkins.blueocean.rest.model;

import io.jenkins.blueocean.rest.annotation.Capability;
import org.kohsuke.stapler.export.Exported;

import java.text.SimpleDateFormat;
import java.util.Date;

import static io.jenkins.blueocean.rest.model.BlueRun.STATE;

/**
 * Pipeline Step resource
 *
 * @author Vivek Pandey
 */
@Capability("io.jenkins.blueocean.rest.model.BluePipelineStep")
public abstract class BluePipelineStep extends Resource{
    public static final String DISPLAY_NAME="displayName";
    public static final String RESULT = "result";
    public static final String START_TIME="startTime";
    public static final String ID = "id";
    public static final String EDGES = "edges";
    public static final String DURATION_IN_MILLIS="durationInMillis";

    @Exported(name = ID)
    public abstract String getId();

    @Exported(name = DISPLAY_NAME)
    public abstract String getDisplayName();

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

}
