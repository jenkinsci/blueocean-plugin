package io.jenkins.blueocean.service.embedded.rest;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import edu.umd.cs.findbugs.annotations.CheckForNull;
import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.model.BuildableItem;
import hudson.model.Job;
import hudson.model.Run;
import io.jenkins.blueocean.commons.ServiceException;
import io.jenkins.blueocean.rest.factory.BluePipelineFactory;
import io.jenkins.blueocean.rest.hal.Link;
import io.jenkins.blueocean.rest.model.BlueOrganization;
import io.jenkins.blueocean.rest.model.BluePipeline;
import io.jenkins.blueocean.rest.model.BlueQueueItem;
import jenkins.model.Jenkins;

import java.util.List;
import java.util.NoSuchElementException;

public class QueueUtil {

    public static BlueQueueItem getQueuedItem(BlueOrganization organization, final hudson.model.Queue.Item item, Job job) {
        for(BlueQueueItem qi: getQueuedItems(organization, job)){
            if(qi.getId() != null && qi.getId().equalsIgnoreCase(Long.toString(item.getId()))){
                return qi;
            }
        }
        return null;
    }

    /**
     * Find a corresponding run for the queueId
     * @param job to search
     * @param queueId of the item
     * @param <T> type of run
     * @return the run or null
     */
    @CheckForNull
    @SuppressWarnings("unchecked")
    public static <T extends Run> T getRun(@NonNull Job job, final long queueId) {
        try {
            return Iterables.find((Iterable<T>) job.getBuilds(), input ->  input != null && input.getQueueId() == queueId);
        } catch ( NoSuchElementException e ) {
            // ignore as maybe we do not have builds
        }
        return null;
    }

    /**
     * This function gets gets a list of all queued items if the job is a buildable item.
     *
     * Note the estimated build number calculation is a guess - job types need not return
     * sequential build numbers.
     *
     * @return List of items newest first
     */
    public static List<BlueQueueItem> getQueuedItems(BlueOrganization organization, Job job) {
        BluePipeline pipeline = (BluePipeline) BluePipelineFactory.resolve(job);
        if(job instanceof BuildableItem && pipeline != null) {
            BuildableItem task = (BuildableItem)job;
            List<hudson.model.Queue.Item> items = Jenkins.getInstance().getQueue().getItems(task);
            List<BlueQueueItem> items2 = Lists.newArrayList();
            for (int i = 0; i < items.size(); i++) {
                Link self = pipeline.getLink().rel("queue").rel(Long.toString(items.get(i).getId()));
                QueueItemImpl queueItem = new QueueItemImpl(
                    organization,
                    items.get(i),
                    pipeline,
                    (items.size() == 1 ? job.getNextBuildNumber() : job.getNextBuildNumber() + i), self, pipeline.getLink());
                items2.add(0, queueItem);
            }

            return items2;
        } else {
            throw new ServiceException.UnexpectedErrorException("This pipeline is not buildable and therefore does not have a queue.");
        }
    }

    private QueueUtil() {}
}
