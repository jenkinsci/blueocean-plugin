package io.jenkins.blueocean.rest.impl.pipeline;

import io.jenkins.blueocean.commons.ServiceException;
import io.jenkins.blueocean.pipeline.api.BlueMultiBranchPipeline;
import io.jenkins.blueocean.rest.Reachable;
import io.jenkins.blueocean.rest.hal.Link;
import io.jenkins.blueocean.rest.model.BluePipeline;
import io.jenkins.blueocean.rest.model.BluePipelineContainer;
import jenkins.branch.MultiBranchProject;
import jenkins.branch.OrganizationFolder;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @author Vivek Pandey
 */
public class MultiBranchPipelineContainerImpl extends BluePipelineContainer {
    private final OrganizationFolder folder;
    private final Link self;

    public MultiBranchPipelineContainerImpl(OrganizationFolder folder, Reachable parent) {
        this.folder = folder;
        this.self = parent.getLink();
    }

    @Override
    public BlueMultiBranchPipeline get(String s) {
        MultiBranchProject mbp =  folder.getItem(s);
        if(mbp == null){
            throw new ServiceException.NotFoundException("Multibranch pipeline "+ s + " not found.");
        }
        return new MultiBranchPipelineImpl(mbp);
    }

    @Override
    public Link getLink() {
        return self;
    }

    @Override
    public Iterator<BluePipeline> iterator() {
        List<BluePipeline> pipelines = new ArrayList<>();
        for(MultiBranchProject mbp:folder.getItems()){
            pipelines.add(new MultiBranchPipelineImpl(mbp));
        }
        return pipelines.iterator();
    }
}
