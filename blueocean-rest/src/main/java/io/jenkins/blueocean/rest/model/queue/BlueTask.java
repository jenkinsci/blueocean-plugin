package io.jenkins.blueocean.rest.model.queue;

import io.jenkins.blueocean.rest.model.BlueRun;
import io.jenkins.blueocean.rest.model.Resource;
import org.apache.tools.ant.Task;
import org.kohsuke.stapler.export.Exported;


public abstract class BlueTask extends Resource {
    public static final String ID = "id";
    public static final String NAME = "name";
    public static final String TYPE = "type";
    public static final String RUN = "run";

    /**
     * @returns Id of queue item. Needs to match up with {@link BlueRun#getQueueId()}.
     */
    @Exported(name = ID)
    public abstract String getId();

    @Exported(name = TYPE)
    public abstract TaskType getType();

    @Exported(name = NAME)
    public abstract String getName();

    @Exported(name = RUN, inline = true)
    public abstract BlueRun getRun();

    public enum TaskType {
        QUEUE_ITEM,
        RUN
    }
}
