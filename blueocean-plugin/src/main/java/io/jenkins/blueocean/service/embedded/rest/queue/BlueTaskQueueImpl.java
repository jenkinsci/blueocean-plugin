package io.jenkins.blueocean.service.embedded.rest.queue;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import hudson.model.Queue;
import io.jenkins.blueocean.rest.model.BlueRun;
import io.jenkins.blueocean.rest.model.queue.BlueTask;
import jenkins.model.Jenkins;

import javax.annotation.Nullable;

/**
 * @author Ivan Meredith
 */
public class BlueTaskQueueImpl extends BlueTask {
    private Queue.Item item;

    public BlueTaskQueueImpl(Queue.Item item) {
        this.item = item;
    }

    @Override
    public String getId() {
        return Long.toString(item.getId());
    }

    @Override
    public TaskType getType() {
        return TaskType.QUEUE_ITEM;
    }

    @Override
    public String getName() {
        return item.getDisplayName();
    }

    @Override
    public BlueRun getRun() {
        return null;
    }

    public static Iterable<BlueTaskQueueImpl> getQueuedTasks() {
        ImmutableList<Queue.Item> items = ImmutableList.copyOf(Jenkins.getInstance().getQueue().getItems());
        return Iterables.transform(items, new Function<Queue.Item, BlueTaskQueueImpl>() {
            @Override
            public BlueTaskQueueImpl apply(@Nullable Queue.Item input) {
                return new BlueTaskQueueImpl(input);
            }
        });
    }
}
