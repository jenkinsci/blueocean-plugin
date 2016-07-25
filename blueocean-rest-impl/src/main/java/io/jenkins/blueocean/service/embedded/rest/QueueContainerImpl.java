package io.jenkins.blueocean.service.embedded.rest;

import com.google.common.collect.Lists;
import hudson.model.BuildableItem;
import hudson.model.Job;
import hudson.model.Queue;
import io.jenkins.blueocean.commons.ServiceException;
import io.jenkins.blueocean.rest.hal.Link;
import io.jenkins.blueocean.rest.model.BluePipeline;
import io.jenkins.blueocean.rest.model.BlueQueueContainer;
import io.jenkins.blueocean.rest.model.BlueQueueItem;
import jenkins.model.Jenkins;

import java.util.Iterator;
import java.util.List;

/**
 * @author Ivan Meredith
 */
public class QueueContainerImpl extends BlueQueueContainer {
    private PipelineImpl pipeline;
    private Job job;

    public QueueContainerImpl(PipelineImpl pipeline) {
        this.pipeline = pipeline;
        this.job = pipeline.job;
    }

    @Override
    public BlueQueueItem get(String name) {
        for (BlueQueueItem blueQueueItem : getQueuedItems(job)) {
            if(name.equals(blueQueueItem.getId())){
                return blueQueueItem;
            }
        }
        return null;
    }


    @Override
    public Iterator<BlueQueueItem> iterator() {
        return getQueuedItems(job).iterator();
    }

    /**
     * This function gets gets a list of all queued items if the job is a buildable item.
     *
     * Note the estimated build number calculation is a guess - job types need not return
     * sequential build numbers.
     *
     * @return List of items newest first
     */
    public static List<BlueQueueItem> getQueuedItems(Job job) {
        BluePipeline pipeline = new PipelineImpl(job);

        if(job instanceof BuildableItem) {
            BuildableItem task = (BuildableItem)job;
            List<Queue.Item> items = Jenkins.getInstance().getQueue().getItems(task);
            List<BlueQueueItem> items2 = Lists.newArrayList();
            for (int i = 0; i < items.size(); i++) {
                items2.add(0, new QueueItemImpl(
                    items.get(i),
                    pipeline,
                    (items.size() == 1 ? job.getNextBuildNumber() : job.getNextBuildNumber() + i)));
            }

            return items2;
        } else {
            throw new ServiceException.UnexpectedErrorException("This pipeline is not buildable and therefore does not have a queue.");
        }
    }

    public static BlueQueueItem getQueuedItem(final Queue.Item item, Job job) {

        for(BlueQueueItem qi:QueueContainerImpl.getQueuedItems(job)){
            if(qi.getId().equalsIgnoreCase(Long.toString(item.getId()))){
                return qi;
            }
        }
        return null;
    }
    @Override
    public Link getLink() {
        return pipeline.getLink().rel("queue");
    }
}
