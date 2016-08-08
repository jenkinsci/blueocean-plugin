package io.jenkins.blueocean.rest.impl.pipeline;

import com.google.common.collect.Lists;
import hudson.model.Job;
import hudson.model.Queue;
import io.jenkins.blueocean.commons.ServiceException;
import io.jenkins.blueocean.rest.hal.Link;
import io.jenkins.blueocean.rest.model.BlueQueueContainer;
import io.jenkins.blueocean.rest.model.BlueQueueItem;
import io.jenkins.blueocean.service.embedded.rest.QueueContainerImpl;
import jenkins.model.Jenkins;

import java.util.Iterator;
import java.util.List;

/**
 * @author Vivek Pandey
 */
public class MultiBranchPipelineQueueContainer extends BlueQueueContainer {

    private final MultiBranchPipelineImpl multiBranchPipeline;
    private final Link self;

    public MultiBranchPipelineQueueContainer(MultiBranchPipelineImpl multiBranchPipeline) {
        this.multiBranchPipeline = multiBranchPipeline;
        this.self = multiBranchPipeline.getLink().rel("queue");
    }

    @Override
    public BlueQueueItem get(String name) {
        try {
            Queue.Item item = Jenkins.getInstance().getQueue().getItem(Long.parseLong(name));
            if(item != null && item.task instanceof Job){
                Job job = ((Job) item.task);
                if(job.getParent() != null && job.getParent().getFullName().equals(multiBranchPipeline.mbp.getFullName())) {
                    return QueueContainerImpl.getQueuedItem(item, job);
                }
            }
        }catch (NumberFormatException e){
            throw new ServiceException.BadRequestExpception("Invalid queue id: "+name+". Must be a number.",e);
        }
        return null;
    }

    @Override
    public Link getLink() {
        return self;
    }

    @Override
    public Iterator<BlueQueueItem> iterator() {
        List<BlueQueueItem> queueItems = Lists.newArrayList();
        for(Object o: multiBranchPipeline.mbp.getItems()) {
            if(o instanceof Job) {
                queueItems.addAll(QueueContainerImpl.getQueuedItems((Job)o));
            }
        }
        return queueItems.iterator();
    }
}
