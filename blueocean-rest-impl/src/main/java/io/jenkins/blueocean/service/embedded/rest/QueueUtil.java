package io.jenkins.blueocean.service.embedded.rest;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import hudson.model.BuildableItem;
import hudson.model.Job;
import hudson.model.Run;
import io.jenkins.blueocean.commons.ServiceException;
import io.jenkins.blueocean.rest.hal.Link;
import io.jenkins.blueocean.rest.hal.LinkResolver;
import io.jenkins.blueocean.rest.model.BlueQueueItem;
import jenkins.model.Jenkins;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public class QueueUtil {

    public static BlueQueueItem getQueuedItem(final hudson.model.Queue.Item item, Job job) {

        for(BlueQueueItem qi: getQueuedItems(job)){
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
    @Nullable
    @SuppressWarnings("unchecked")
    public static <T extends Run> T getRun(@Nonnull Job job, final long queueId) {
        return Iterables.find((Iterable<T>) job.getBuilds(), new Predicate<Run>() {
            @Override
            public boolean apply(@Nullable Run input) {
                return input != null && input.getQueueId() == queueId;
            }
        }, null);
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
            List<hudson.model.Queue.Item> items = Jenkins.getInstance().getQueue().getItems(task);
            List<BlueQueueItem> items2 = Lists.newArrayList();
            for (int i = 0; i < items.size(); i++) {
                Link self = pipelineLink.rel("queue").rel(Long.toString(items.get(i).getId()));
                items2.add(0, new QueueItemImpl(
                    items.get(i),
                    job.getName(),
                    (items.size() == 1 ? job.getNextBuildNumber() : job.getNextBuildNumber() + i), self, pipelineLink));
            }

            return items2;
        } else {
            throw new ServiceException.UnexpectedErrorException("This pipeline is not buildable and therefore does not have a queue.");
        }
    }

    private QueueUtil() {}
}
