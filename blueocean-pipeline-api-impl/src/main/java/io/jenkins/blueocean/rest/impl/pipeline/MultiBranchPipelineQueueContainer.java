package io.jenkins.blueocean.rest.impl.pipeline;

import hudson.model.Job;
import hudson.model.Queue;
import io.jenkins.blueocean.commons.ServiceException;
import io.jenkins.blueocean.rest.hal.Link;
import io.jenkins.blueocean.rest.model.BluePipeline;
import io.jenkins.blueocean.rest.model.BlueQueueContainer;
import io.jenkins.blueocean.rest.model.BlueQueueItem;
import io.jenkins.blueocean.service.embedded.rest.QueueUtil;
import jenkins.model.Jenkins;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

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
            Queue.Item item = Jenkins.get().getQueue().getItem(Long.parseLong(name));
            if(item != null && item.task instanceof Job){
                Job job = ((Job) item.task);
                if(job.getParent() != null && job.getParent().getFullName().equals(multiBranchPipeline.mbp.getFullName())) {
                    return QueueUtil.getQueuedItem(multiBranchPipeline.getOrganization(), item, job);
                }
            }
        }catch (NumberFormatException e){
            throw new ServiceException.BadRequestException("Invalid queue id: "+name+". Must be a number.",e);
        }
        return null;
    }

    @Override
    public Link getLink() {
        return self;
    }

    @Override
    public Iterator<BlueQueueItem> iterator() {
        List<BlueQueueItem> queueItems = new ArrayList<>();
        for(Object o: multiBranchPipeline.mbp.getItems()) {
            if(o instanceof Job) {
                queueItems.addAll(QueueUtil.getQueuedItems(multiBranchPipeline.getOrganization(), (Job)o));
            }
        }
        return queueItems.iterator();
    }

    @Override
    public Iterator<BlueQueueItem> iterator(int start, int limit) {
        List<BluePipeline> branches = StreamSupport.stream(multiBranchPipeline.getBranches().spliterator(), false)
            .sorted(( o1, o2 ) -> PipelineRunImpl.LATEST_RUN_START_TIME_COMPARATOR.compare(o1.getLatestRun(), o2.getLatestRun()))
            .collect(Collectors.toList());
        int l = branches.size() > 0 && limit/branches.size() > 0 ? limit/branches.size() : 1;

        int s=0;
        if(start > 0) {
            s = Math.max(start - l, 0);
        }

        List<BlueQueueItem> c = new ArrayList<>();
        int count = 0;
        int retry = 0;
        while(retry < 5 && count < limit) {
            for (BluePipeline b : branches) {
                Iterator<BlueQueueItem> it = b.getQueue().iterator(s, l);
                while (it.hasNext()) {
                    count++;
                    c.add(it.next());
                }
            }
            retry++;
        }

        c.sort(( o1, o2 ) -> o2.getQueuedTime().compareTo(o1.getQueuedTime()));
        return c.iterator();
    }

}
