package io.jenkins.blueocean.service.embedded.rest.queue;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import hudson.model.Job;
import hudson.model.Run;
import hudson.util.RunList;
import io.jenkins.blueocean.rest.model.BlueRun;
import io.jenkins.blueocean.rest.model.queue.BlueTask;
import io.jenkins.blueocean.service.embedded.rest.AbstractRunImpl;
import jenkins.model.Jenkins;

import javax.annotation.Nullable;

/**
 * @author ivan Meredith
 */
public class BlueTaskRunImpl extends BlueTask {
    private Run run;

    public BlueTaskRunImpl(Run run) {
        this.run = run;
    }

    public String getId() {
        return Long.toString(run.getQueueId());
    }

    @Override
    public TaskType getType() {
        return TaskType.RUN;
    }

    @Override
    public String getName() {
        return run.getParent().getName();
    }

    @Override
    public BlueRun getRun() {
        return AbstractRunImpl.getBlueRun(run);
    }

    public static Iterable<BlueTaskRunImpl> getRunningTasks() {
        RunList runlist = new RunList(Jenkins.getInstance().getAllItems(Job.class));

        return Iterables.transform(runlist, new Function<Run, BlueTaskRunImpl>() {
            @Override
            public BlueTaskRunImpl apply(@Nullable Run input) {
                return new BlueTaskRunImpl(input);
            }
        });
    }
}
