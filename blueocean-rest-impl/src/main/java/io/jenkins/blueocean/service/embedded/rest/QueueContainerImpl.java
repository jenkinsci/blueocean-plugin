package io.jenkins.blueocean.service.embedded.rest;

import com.google.common.collect.Lists;
import hudson.model.BuildableItem;
import hudson.model.Job;
import hudson.model.Queue;
import io.jenkins.blueocean.commons.ServiceException;
import io.jenkins.blueocean.rest.hal.Link;
import io.jenkins.blueocean.rest.hal.LinkResolver;
import io.jenkins.blueocean.rest.model.BlueQueueContainer;
import io.jenkins.blueocean.rest.model.BlueQueueItem;
import io.jenkins.blueocean.rest.model.BlueRun;
import jenkins.model.Jenkins;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import org.kohsuke.stapler.Stapler;
import org.kohsuke.stapler.StaplerResponse;

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
        for (BlueQueueItem blueQueueItem : getQueuedItems(job)) {
            if(name.equals(blueQueueItem.getId())){
                return blueQueueItem;
            }
        }
        // JENKINS-38540 - To make this consistent with the activity API, check the runs, too
        String runId = findQueueInRuns(name);
        if (runId != null) {
            try {
                StaplerResponse rsp = Stapler.getCurrentResponse();
                // Send a redirect, not sure the specific code which would be best. substring to fix double slash
                rsp.sendRedirect(Jenkins.getInstance().getRootUrl() + pipeline.getLink().toString().substring(1) + "runs/" + runId);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            return null;
        }
        return null;
    }

    /**
     * Finds a queue in the runs based on the queueId
     */
    private String findQueueInRuns(String name) {
        try {
            long queueId = Long.parseLong(name);
            for (BlueRun run : this.pipeline.getRuns()) {
                if (run instanceof AbstractRunImpl<?>) {
                    if (queueId == ((AbstractRunImpl)run).getQueueId()) {
                        return run.getId();
                    }
                }
            }
        } catch (NumberFormatException e) {
            // not a queue id!
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
        Link pipelineLink = LinkResolver.resolveLink(job);
        if(job instanceof BuildableItem) {
            BuildableItem task = (BuildableItem)job;
            List<Queue.Item> items = Jenkins.getInstance().getQueue().getItems(task);
            List<BlueQueueItem> items2 = Lists.newArrayList();
            for (int i = 0; i < items.size(); i++) {
                Link self = pipelineLink.rel("queue").rel(Long.toString(items.get(i).getId()));
                items2.add(0, new QueueItemImpl(
                    items.get(i),
                    job.getName(),
                    (items.size() == 1 ? job.getNextBuildNumber() : job.getNextBuildNumber() + i), self));
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
