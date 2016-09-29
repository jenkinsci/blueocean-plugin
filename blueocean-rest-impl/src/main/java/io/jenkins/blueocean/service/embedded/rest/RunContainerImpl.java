package io.jenkins.blueocean.service.embedded.rest;

import hudson.model.Cause;
import hudson.model.CauseAction;
import hudson.model.Item;
import hudson.model.Job;
import hudson.model.Queue;
import hudson.model.Run;
import hudson.model.queue.ScheduleResult;
import hudson.util.RunList;
import io.jenkins.blueocean.commons.ServiceException;
import io.jenkins.blueocean.rest.hal.Link;
import io.jenkins.blueocean.rest.hal.Links;
import io.jenkins.blueocean.rest.model.BluePipeline;
import io.jenkins.blueocean.rest.model.BlueQueueItem;
import io.jenkins.blueocean.rest.model.BlueRun;
import io.jenkins.blueocean.rest.model.BlueRunContainer;
import jenkins.model.Jenkins;

import javax.annotation.Nonnull;

import org.kohsuke.stapler.Stapler;
import org.kohsuke.stapler.StaplerResponse;

import java.io.IOException;
import java.util.Iterator;

/**
 * @author Vivek Pandey
 */
public class RunContainerImpl extends BlueRunContainer {

    private final Job job;
    private final BluePipeline pipeline;

    public RunContainerImpl(@Nonnull BluePipeline pipeline, @Nonnull Job job) {
        this.job = job;
        this.pipeline = pipeline;
    }

    @Override
    public Link getLink() {
        return pipeline.getLink().rel("runs");
    }

    @Override
    public BlueRun get(String name) {
        RunList<? extends hudson.model.Run> runList = job.getBuilds();

        hudson.model.Run run = null;
        if (name != null) {
            for (hudson.model.Run r : runList) {
                if (r.getId().equals(name)) {
                    run = r;
                    break;
                }
            }
            if (run == null) {
                // JENKINS-38540 - To make this consistent with the activity API, check the queue
                String queuedRun = findRunInQueue(name);
                if (queuedRun != null) {
                    try {
                        StaplerResponse rsp = Stapler.getCurrentResponse();
                        // Send a 302, temporary redirect. substring to fix double slash
                        rsp.sendRedirect(Links.ensureTrailingSlash(Jenkins.getInstance().getRootUrl() + pipeline.getLink().toString().substring(1) + "queue/" + queuedRun));
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    return null;
                }
            }
            if (run == null) {
                throw new ServiceException.NotFoundException(
                    String.format("Run %s not found in organization %s and pipeline %s",
                        name, pipeline.getOrganization(), job.getName()));
            }
        } else {
            run = runList.getLastBuild();
        }
        return  AbstractRunImpl.getBlueRun(run, pipeline);
    }

    /**
     * Finds a run in the queue based on the expectedBuildNumber, returns the queue id if found
     */
    private String findRunInQueue(String name) {
        try {
            int expectedBuildNumber = Integer.parseInt(name);
            for (BlueQueueItem i : this.pipeline.getQueue()) {
                if (expectedBuildNumber == i.getExpectedBuildNumber()) {
                    return i.getId();
                }
            }
        } catch(NumberFormatException e) {
            // not an expectedBuildNumber
        }
        return null;
    }

    @Override
    public Iterator<BlueRun> iterator() {
        return RunSearch.findRuns(job, pipeline.getLink()).iterator();
    }

    @Override
    public BluePipeline getPipeline(String name) {
        return pipeline;
    }

    /**
     * Schedules a build. If build already exists in the queue and the pipeline does not
     * support running multiple builds at the same time, return a reference to the existing
     * build.
     *
     * @return Queue item.
     */
    @Override
    public BlueQueueItem create() {
        job.checkPermission(Item.BUILD);
        if (job instanceof Queue.Task) {
            ScheduleResult scheduleResult = Jenkins.getInstance()
                .getQueue()
                .schedule2((Queue.Task)job, 0, new CauseAction(new Cause.UserIdCause()));

            if(scheduleResult.isAccepted()) {
                final Queue.Item item = scheduleResult.getItem();

                BlueQueueItem queueItem = QueueContainerImpl.getQueuedItem(item, job);

                if (queueItem == null) {
                    throw new ServiceException.UnexpectedErrorException("The queue item does not exist in the queue");
                } else {
                    return queueItem;
                }
            } else {
                throw new ServiceException.UnexpectedErrorException("Queue item request was not accepted");
            }
        } else {
            throw new ServiceException.NotImplementedException("This pipeline type does not support being queued.");
        }
    }
}
