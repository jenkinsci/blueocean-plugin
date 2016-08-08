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
    private final Queue.Item item;
    private final String pipelineName;
    private final Link self;
    private final int expectedBuildNumber;

    public QueueItemImpl(Queue.Item item, BluePipeline pipeline, int expectedBuildNumber) {
        this(item,
            pipeline.getName(),expectedBuildNumber,
            pipeline.getQueue().getLink().rel(Long.toString(item.getId())));
    }

    public QueueItemImpl(Queue.Item item, String name, int expectedBuildNumber, Link self) {
        this.item = item;
        this.pipelineName = name;
        this.expectedBuildNumber = expectedBuildNumber;
        this.self = self;
    }

    @Override
    public String getId() {
        return Long.toString(item.getId());
    }

    @Override
    public String getOrganization() {
        return OrganizationImpl.INSTANCE.getName();
    }

    @Override
    public String getPipeline() {
        return pipelineName;
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
        return self;
    }
}
