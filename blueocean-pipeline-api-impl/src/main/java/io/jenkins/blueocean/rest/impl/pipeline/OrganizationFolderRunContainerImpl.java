package io.jenkins.blueocean.rest.impl.pipeline;

import hudson.model.Cause;
import hudson.model.CauseAction;
import hudson.model.Item;
import hudson.model.Queue;
import io.jenkins.blueocean.rest.Reachable;
import io.jenkins.blueocean.rest.hal.Link;
import io.jenkins.blueocean.rest.model.BlueRun;
import io.jenkins.blueocean.rest.model.BlueRunContainer;
import io.jenkins.blueocean.service.embedded.rest.QueueItemImpl;
import org.kohsuke.stapler.StaplerRequest;

import edu.umd.cs.findbugs.annotations.NonNull;
import java.util.Collections;
import java.util.Iterator;

/**
 * @author Vivek Pandey
 */
public class OrganizationFolderRunContainerImpl extends BlueRunContainer {


    private final OrganizationFolderPipelineImpl pipeline;
    private final Link self;
    private final OrganizationFolderRunImpl run;

    public OrganizationFolderRunContainerImpl(OrganizationFolderPipelineImpl pipeline, Reachable parent) {
        this.pipeline = pipeline;
        this.self = parent.getLink().rel("runs");
        this.run = new OrganizationFolderRunImpl(pipeline, this);
    }

    @Override
    public Link getLink() {
        return self;
    }

    @Override
    public BlueRun create(StaplerRequest request) {
        pipeline.folder.checkPermission(Item.BUILD);
        Queue.Item queueItem = pipeline.folder.scheduleBuild2(0, new CauseAction(new Cause.UserIdCause()));
        if(queueItem == null){ // possible folder.isBuildable() was false due to no repo fetched yet
            return null;
        }
        return new QueueItemImpl(
                pipeline.getOrganization(),
                queueItem,
                pipeline,
                1
        ).toRun();
    }

    @Override
    public BlueRun get(String name) {
        if(name.equals(OrganizationFolderRunImpl.RUN_ID)){
            return new OrganizationFolderRunImpl(pipeline, this);
        }
        return null;
    }

    @Override
    @NonNull
    public Iterator<BlueRun> iterator() {
        return Collections.singleton((BlueRun)run).iterator();
    }
}
