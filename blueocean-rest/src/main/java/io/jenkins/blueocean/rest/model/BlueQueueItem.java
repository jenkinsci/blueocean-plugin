package io.jenkins.blueocean.rest.model;

import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.export.ExportedBean;

import java.text.SimpleDateFormat;
import java.util.Date;

import static io.jenkins.blueocean.commons.JsonConverter.DATE_FORMAT_STRING;


/**
 * This class models an item waiting in the queue for an executor so that
 * it can run.
 *
 * @author Ivan Meredith
 */
public abstract class BlueQueueItem extends Resource {

    public static final String QUEUED_TIME = "qeueudTime";

    /**
     * @return Id of the item in the queue. Much be unique in the queue of a pipeline
     */
    @Exported
    public abstract String getId();

    /**
     *
     * @return pipeline this queued item belongs too
     */
    @Exported
    public abstract String getPipeline();

    /**
     *
     * @return Time the item entered the queue.
     */
    public abstract Date getQueuedTime();

    @Exported(name=QUEUED_TIME)
    public final String getQueuedTimeString(){
        return new SimpleDateFormat(DATE_FORMAT_STRING).format(getQueuedTime());
    }
    /**
     *
     * @return  The expected build number of the build. This may change.
     */
    @Exported
    public abstract int getExpectedBuildNumber();
}
