package io.jenkins.blueocean.rest.model;

import io.jenkins.blueocean.rest.annotation.Capability;
import org.kohsuke.stapler.WebMethod;
import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.verb.DELETE;

import java.text.SimpleDateFormat;
import java.util.Date;

import static io.jenkins.blueocean.commons.JsonConverter.DATE_FORMAT_STRING;
import static io.jenkins.blueocean.rest.model.KnownCapabilities.BLUE_QUEUE_ITEM;


/**
 * This class models an item waiting in the queue for an executor so that
 * it can run.
 *
 * @author Ivan Meredith
 */
@Capability(BLUE_QUEUE_ITEM)
public abstract class BlueQueueItem extends Resource {

    public static final String QUEUED_TIME = "queuedTime";
    private static final String CAUSE_OF_BLOCKAGE = "causeOfBlockage";

    /**
     * @return Id of the item in the queue. Much be unique in the queue of a pipeline
     */
    @Exported
    public abstract String getId();

    @Exported
    public abstract String getOrganization();
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

    /**
     * Remove a queued item
     */
    @WebMethod(name="") @DELETE
    public abstract void delete();

    /**
     * @return Gives reason of blockage if run is in QUEUED state
     */
    @Exported(name = CAUSE_OF_BLOCKAGE)
    public abstract String getCauseOfBlockage();

    /**
     * @return a run object representing this queued item
     */
    public abstract BlueRun toRun();
}
