package io.jenkins.blueocean.service.embedded.rest.queue;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import hudson.Extension;
import io.jenkins.blueocean.rest.model.Containers;
import io.jenkins.blueocean.rest.model.queue.BlueTask;
import io.jenkins.blueocean.rest.model.queue.BlueTaskContainer;

import javax.annotation.Nullable;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;

/**
 * @auther Ivan Meredith
 */
@Extension
public class TaskContainerImpl extends BlueTaskContainer {

    @Override
    public BlueTask get(final String id) {
        BlueTask task = null;

        try {
            return Iterables.find(BlueTaskQueueImpl.getQueuedTasks(), new Predicate<BlueTaskQueueImpl>() {
                @Override
                public boolean apply(@Nullable BlueTaskQueueImpl input) {
                    return input.getId().equalsIgnoreCase(id);
                }
            });
        } catch (NoSuchElementException e) {
            try {
                return Iterables.find(BlueTaskRunImpl.getRunningTasks(), new Predicate<BlueTaskRunImpl>() {
                    @Override
                    public boolean apply(@Nullable BlueTaskRunImpl input) {
                        return id.equalsIgnoreCase(input.getId());
                    }
                });
            } catch(NoSuchElementException ne) {
                return null;
            }
        }
    }

    @Override
    public Iterator<BlueTask> iterator() {
        Iterable<BlueTask> tasks = Iterables.concat(BlueTaskQueueImpl.getQueuedTasks(), BlueTaskRunImpl.getRunningTasks());

        return tasks.iterator();
    }
}

