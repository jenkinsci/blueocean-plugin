package io.jenkins.blueocean.rest.impl.pipeline;

import hudson.model.Job;
import hudson.model.Queue;
import io.jenkins.blueocean.commons.ServiceException;
import io.jenkins.blueocean.rest.hal.Link;
import io.jenkins.blueocean.rest.model.BluePipeline;
import io.jenkins.blueocean.rest.model.BlueQueueContainer;
import io.jenkins.blueocean.rest.model.BlueQueueItem;
import io.jenkins.blueocean.service.embedded.rest.QueueContainerImpl;
import io.jenkins.blueocean.service.embedded.rest.QueueItemImpl;
import jenkins.model.Jenkins;
import org.jenkinsci.plugins.workflow.support.steps.ExecutorStepExecution;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

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
            Queue.Item item = Jenkins.getActiveInstance().getQueue().getItem(Long.parseLong(name));
            if(item != null){
                BranchImpl pipeline = (BranchImpl) multiBranchPipeline.getBranches().get(item.task.getOwnerTask().getName());
                if(pipeline != null) {

                    if(item.task instanceof ExecutorStepExecution.PlaceholderTask) {
                        ExecutorStepExecution.PlaceholderTask task = (ExecutorStepExecution.PlaceholderTask) item.task;
                        if(task.run() == null){
                            return QueueContainerImpl.getQueuedItem(item, pipeline.job);
                        }else{
                            return new QueueItemImpl(item, item.task.getOwnerTask().getName(), task.run().getNumber(),
                                self.rel(String.valueOf(item.getId())));
                        }
                    }

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
        final List<BlueQueueItem> items = new ArrayList<>();
        Map<String,List<Queue.Item>> queueMap = new HashMap<>();

        for(Queue.Item item: Jenkins.getActiveInstance().getQueue().getItems()){
            if(item.task instanceof ExecutorStepExecution.PlaceholderTask){
                ExecutorStepExecution.PlaceholderTask task = (ExecutorStepExecution.PlaceholderTask) item.task;
                String ownerTaskName = task.getOwnerTask().getName();
                List<Queue.Item> its = queueMap.get(task.getOwnerTask().getName());
                if(its == null){
                    its = new ArrayList<>();
                    queueMap.put(ownerTaskName,its);
                }
                its.add(item);
            }
        }
        for(final BluePipeline p:multiBranchPipeline.getBranches()){
            Job job =  ((BranchImpl)p).job;
            List<Queue.Item> its = queueMap.get(job.getName());
            if(its == null || its.isEmpty()){
                continue;
            }
            int count=0;
            for(Queue.Item item:its){
                ExecutorStepExecution.PlaceholderTask task = (ExecutorStepExecution.PlaceholderTask) item.task;
                if(task != null){
                    int runNumber;
                    if(task.run() == null){
                        runNumber = job.getNextBuildNumber() + count;
                        count++;
                    }else{
                        runNumber = task.run().getNumber();
                    }
                    items.add(new QueueItemImpl(item,p.getName(),
                        runNumber, self.rel(String.valueOf(item.getId()))));
                }
            }
        }
        return items.iterator();
    }
}
