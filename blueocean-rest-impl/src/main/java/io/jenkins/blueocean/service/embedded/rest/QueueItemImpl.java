package io.jenkins.blueocean.service.embedded.rest;

import hudson.model.Queue;
import io.jenkins.blueocean.rest.hal.Link;
import io.jenkins.blueocean.rest.model.BluePipeline;
import io.jenkins.blueocean.rest.model.BlueQueueItem;

import java.util.Date;

/**
 * @author ivan Meredith
 */
public class QueueItemImpl extends BlueQueueItem {
    private Queue.Item item;
    private BluePipeline pipeline;
    private int expectedBuildNumber;

    public QueueItemImpl(Queue.Item item, BluePipeline pipeline, int expectedBuildNumber) {
        this.item = item;
        this.pipeline = pipeline;
        this.expectedBuildNumber = expectedBuildNumber;
    }

    @Override
    public String getId() {
        return Long.toString(item.getId());
    }

    @Override
    public String getPipeline() {
        return pipeline.getName();
    }

    @Override
    public Date getQueuedTime() {
        return new Date(item.getInQueueSince());
    }

    @Override
    public int getExpectedBuildNumber() {
        return expectedBuildNumber;
    }

    @Override
    public Link getLink() {
        return pipeline.getQueue().getLink().rel(getId());
    }
}
