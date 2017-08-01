package io.jenkins.blueocean.rest.impl.pipeline;

import hudson.model.ItemGroup;
import io.jenkins.blueocean.commons.ServiceException;
import io.jenkins.blueocean.rest.Reachable;
import io.jenkins.blueocean.rest.factory.organization.OrganizationFactory;
import io.jenkins.blueocean.rest.hal.Link;
import io.jenkins.blueocean.rest.model.BlueMultiBranchPipeline;
import io.jenkins.blueocean.rest.model.BlueOrganization;
import io.jenkins.blueocean.rest.model.BluePipeline;
import io.jenkins.blueocean.rest.model.BluePipelineContainer;
import jenkins.branch.MultiBranchProject;
import jenkins.branch.OrganizationFolder;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @author Vivek Pandey
 */
public class MultiBranchPipelineContainerImpl extends BluePipelineContainer {
    private final OrganizationFolder folder;
    private final Link self;

    public MultiBranchPipelineContainerImpl(BlueOrganization organization, OrganizationFolder folder, Reachable parent) {
        super(organization);
        this.folder = folder;
        this.self = parent.getLink();
    }

    @Override
    public BlueMultiBranchPipeline get(String s) {
        MultiBranchProject mbp =  folder.getItem(s);
        if(mbp == null){
            return null;
        }
        return new MultiBranchPipelineImpl(organization, mbp);
    }

    @Override
    public Link getLink() {
        return self;
    }

    @Override
    @Nonnull
    public Iterator<BluePipeline> iterator() {
        List<BluePipeline> pipelines = new ArrayList<>();
        for(MultiBranchProject mbp:folder.getItems()){
            pipelines.add(new MultiBranchPipelineImpl(organization, mbp));
        }
        return pipelines.iterator();
    }
}
