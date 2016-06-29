package io.jenkins.blueocean.service.embedded.rest;

import hudson.model.Job;
import hudson.model.Queue;
import io.jenkins.blueocean.commons.ServiceException;
import io.jenkins.blueocean.rest.hal.Link;
import io.jenkins.blueocean.rest.model.BluePipeline;
import io.jenkins.blueocean.rest.model.BlueQueueContainer;
import io.jenkins.blueocean.rest.model.BlueQueueItem;
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
                PipelineImpl pipeline = (PipelineImpl) multiBranchPipeline.getBranches().get(item.task.getOwnerTask().getName());
                if(pipeline != null) {
                    int runId=0;
                    if(item.task instanceof ExecutorStepExecution.PlaceholderTask) {
                        ExecutorStepExecution.PlaceholderTask task = (ExecutorStepExecution.PlaceholderTask) item.task;
                        runId = task.run().getNumber();
                    }
                    return new QueueItemImpl(item, item.task.getOwnerTask().getName(), runId,
                        self.rel(String.valueOf(item.getId())));
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
            Job job =  ((PipelineImpl)p).job;
            List<Queue.Item> its = queueMap.get(job.getName());
            if(its == null || its.isEmpty()){
                continue;
            }
            for(Queue.Item item:its){
                ExecutorStepExecution.PlaceholderTask task = (ExecutorStepExecution.PlaceholderTask) item.task;
                if(task != null){
                    items.add(new QueueItemImpl(item,p.getName(),
                        task.run().getNumber(), self.rel(String.valueOf(item.getId()))));
                }
            }
        }
        return items.iterator();
    }
}
