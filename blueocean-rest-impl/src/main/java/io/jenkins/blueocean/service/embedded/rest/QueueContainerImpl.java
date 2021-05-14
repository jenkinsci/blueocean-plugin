package io.jenkins.blueocean.service.embedded.rest;

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.model.Job;
import io.jenkins.blueocean.rest.hal.Link;
import io.jenkins.blueocean.rest.model.BlueQueueContainer;
import io.jenkins.blueocean.rest.model.BlueQueueItem;

import java.util.Iterator;

/**
 * @author Ivan Meredith
 */
public class QueueContainerImpl extends BlueQueueContainer {
    private AbstractPipelineImpl pipeline;
    private Job job;

    public QueueContainerImpl(AbstractPipelineImpl pipeline) {
        this.pipeline = pipeline;
        this.job = pipeline.getJob();
    }

    @Override
    public BlueQueueItem get(String name) {
        for (BlueQueueItem blueQueueItem : QueueUtil.getQueuedItems(pipeline.organization, job)) {
            if(name.equals(blueQueueItem.getId())){
                return blueQueueItem;
            }
        }
        return null;
    }


    @Override
    @NonNull
    public Iterator<BlueQueueItem> iterator() {
        return QueueUtil.getQueuedItems(pipeline.organization, job).iterator();
    }

    @Override
    public Link getLink() {
        return pipeline.getLink().rel("queue");
    }
}
