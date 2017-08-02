package io.jenkins.blueocean.rest.impl.pipeline;

import com.google.common.collect.ImmutableList;
import io.jenkins.blueocean.rest.Reachable;
import io.jenkins.blueocean.rest.hal.Link;
import io.jenkins.blueocean.rest.model.BlueRun;
import io.jenkins.blueocean.rest.model.BlueRunContainer;
import org.kohsuke.stapler.StaplerRequest;

import javax.annotation.Nonnull;
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
        return null;
    }

    @Override
    public BlueRun get(String name) {
        if(name.equals(OrganizationFolderRunImpl.RUN_ID)){
            return new OrganizationFolderRunImpl(pipeline, this);
        }
        return null;
    }

    @Override
    @Nonnull
    public Iterator<BlueRun> iterator() {
        return ImmutableList.of((BlueRun)run).iterator();
    }
}
