package io.jenkins.blueocean.service.embedded.rest;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import hudson.model.*;
import io.jenkins.blueocean.commons.ServiceException;
import io.jenkins.blueocean.rest.model.*;
import jenkins.model.Jenkins;

import javax.annotation.Nullable;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

/**
 * @author Ivan Meredith
 */
public class QueueContainerImpl extends BlueQueueContainer {
    private PipelineImpl pipeline;
    private Job job;

    public QueueContainerImpl(PipelineImpl pipeline, Job job) {
        this.pipeline = pipeline;
        this.job = job;
    }

    @Override
    public BlueQueueItem get(String name) {
        for (BlueQueueItem blueQueueItem : getQueuedItems()) {
            if(name.equals(blueQueueItem.getId())){
                return blueQueueItem;
            }
        }
        return null;
    }

    @Override
    public Iterator<BlueQueueItem> iterator() {
        return getQueuedItems().iterator();
    }

    /**
     * This function gets gets a list of all queued items if the job is a buildable item.
     *
     * Note the estimated build number calculation is a guess - job types need not return
     * sequential build numbers.
     *
     * @return List of items newest first
     */
    private List<BlueQueueItem> getQueuedItems() {
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
}
