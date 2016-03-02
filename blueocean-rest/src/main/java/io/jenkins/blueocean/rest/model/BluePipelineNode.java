package io.jenkins.blueocean.rest.model;

import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.export.ExportedBean;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * @author Vivek Pandey
 */
public abstract class BluePipelineNode extends Resource{

    @Exported
    public abstract String getId();

    @Exported
    public abstract String getDisplayName();

    @Exported
    public abstract Status getStatus();

    public abstract Date getStartTime();

    @Exported(inline = true)
    public abstract List<Edge> getEdges();

    @Exported(name = "startTime")
    public final String getStartTimeString(){
        return new SimpleDateFormat(BlueRun.DATE_FORMAT_STRING).format(getStartTime());
    }


    @ExportedBean
    public abstract static class Edge{
        @Exported
        public abstract String getId();

        @Exported
        public abstract long getDurationInMillis();
    }

    public enum Status{
        NOT_EXECUTED,
        ABORTED,
        SUCCESS,
        IN_PROGRESS,
        PAUSED_PENDING_INPUT,
        FAILED;
    }

}
