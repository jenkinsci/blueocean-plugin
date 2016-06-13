package io.jenkins.blueocean.rest.model;

import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.export.ExportedBean;

import java.util.Date;

/**
 * This class models an item waiting in the queue for an executor so that
 * it can run.
 *
 * @author Ivan Meredith
 */
@ExportedBean
public abstract class BlueQueueItem {

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
    @Exported
    public abstract Date getQueuedTime();

    /**
     *
     * @return The expected build number of the build. This may change.
     */
    @Exported
    public abstract int getExpectedBuildNumber();
}
