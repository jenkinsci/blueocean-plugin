package io.jenkins.blueocean.service.embedded.rest;

import hudson.model.Item;
import hudson.model.Queue;
import io.jenkins.blueocean.commons.ServiceException;
import io.jenkins.blueocean.rest.factory.organization.OrganizationFactory;
import io.jenkins.blueocean.rest.hal.Link;
import io.jenkins.blueocean.rest.hal.Links;
import io.jenkins.blueocean.rest.model.BlueOrganization;
import io.jenkins.blueocean.rest.model.BluePipeline;
import io.jenkins.blueocean.rest.model.BlueQueueItem;
import io.jenkins.blueocean.rest.model.BlueRun;
import io.jenkins.blueocean.rest.model.BlueRun.BlueCause;
import io.jenkins.blueocean.rest.model.BlueRun.BlueRunResult;
import io.jenkins.blueocean.rest.model.BlueRun.BlueRunState;
import io.jenkins.blueocean.service.embedded.rest.AbstractRunImpl.BlueCauseImpl;
import jenkins.model.Jenkins;
import org.kohsuke.stapler.export.Exported;

import java.util.Collection;
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
    private final BlueOrganization organization;

    public QueueItemImpl(BlueOrganization organization, Queue.Item item, BluePipeline pipeline, int expectedBuildNumber) {
        this(organization,
            item,
            pipeline.getName(),expectedBuildNumber,
            pipeline.getQueue().getLink().rel(Long.toString(item.getId())),
            pipeline.getLink());
    }

    QueueItemImpl(BlueOrganization organization, Queue.Item item, String name, int expectedBuildNumber, Link self, Link parent) {
        this.organization = organization;
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
        return organization.getName();
    }

    @Override
    public String getPipeline() {
        return pipelineName;
    }

    @Override
    public Date getQueuedTime() {
        return new Date(item.getInQueueSince());
    }

    @Exported(name=QUEUED_TIME)
    public String getQueuedTimeString(){
        return AbstractRunImpl.DATE_FORMAT.print(getQueuedTime().getTime());
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
    public Collection<BlueCause> getCauses() {
        return BlueCauseImpl.getCauses(item.getCauses());
    }

    @Override
    public String getCauseOfBlockage() {
        return item.getCauseOfBlockage().toString();
    }

    @Override
    public BlueRun toRun() {
        return new QueuedBlueRun(BlueRunState.QUEUED, BlueRunResult.UNKNOWN, this, parent);
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
