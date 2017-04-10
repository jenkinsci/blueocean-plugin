package io.jenkins.blueocean.service.embedded.rest;

import hudson.model.Queue;
import io.jenkins.blueocean.commons.ServiceException;
import io.jenkins.blueocean.rest.hal.Link;
import io.jenkins.blueocean.rest.hal.Links;
import io.jenkins.blueocean.rest.model.BluePipeline;
import io.jenkins.blueocean.rest.model.BlueQueueItem;
import jenkins.model.Jenkins;

import java.util.Date;

/**
 * @author ivan Meredith
 */
public class QueueItemImpl extends BlueQueueItem {
    private final Queue.Item item;
    private final String pipelineName;
    private final Link self;
    private final Link parent;
    private final int expectedBuildNumber;

    public QueueItemImpl(Queue.Item item, BluePipeline pipeline, int expectedBuildNumber) {
        this(item,
            pipeline.getName(),expectedBuildNumber,
            pipeline.getQueue().getLink().rel(Long.toString(item.getId())),
            pipeline.getLink());
    }

    public QueueItemImpl(Queue.Item item, String name, int expectedBuildNumber, Link self, Link parent) {
        this.item = item;
        this.pipelineName = name;
        this.expectedBuildNumber = expectedBuildNumber;
        this.self = self;
        this.parent = parent;
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
    public void delete(){
        if(!item.hasCancelPermission()){
            throw new ServiceException.ForbiddenException(String.format("Not authorized to stop queue: %s", getId()));
        }

        Jenkins.getInstance().getQueue().cancel(item);
    }

    @Override
    public String getCauseOfBlockage() {
        return item.getCauseOfBlockage().toString();
    }

    @Override
    public Link getLink() {
        return self;
    }

    @Override
    public Links getLinks() {
        return super.getLinks().add("parent",   parent);
    }
}
