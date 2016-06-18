package io.jenkins.blueocean.service.embedded.rest;

import hudson.model.BuildableItem;
import hudson.model.Item;
import hudson.model.ItemGroup;
import hudson.model.Job;
import io.jenkins.blueocean.commons.ServiceException;
import io.jenkins.blueocean.rest.hal.Link;
import io.jenkins.blueocean.rest.model.BluePipeline;
import io.jenkins.blueocean.rest.model.BluePipelineContainer;
import jenkins.branch.MultiBranchProject;
import jenkins.model.Jenkins;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * @author Vivek Pandey
 */
public class PipelineContainerImpl extends BluePipelineContainer {
    private final @Nonnull ItemGroup itemGroup;

    public PipelineContainerImpl() {
        this.itemGroup = Jenkins.getInstance();
    }

    public PipelineContainerImpl(ItemGroup itemGroup) {
        this.itemGroup = itemGroup;
    }

    @Override
    public Link getLink() {
        return OrganizationImpl.INSTANCE.getLink().rel("pipelines");
    }

    @Override
    public BluePipeline get(String name) {
        Item item;
        item = itemGroup.getItem(name);

        if(item == null){
            throw new ServiceException.NotFoundException(String.format("Pipeline %s not found", name));
        }
        return get(item);
    }

    public BluePipeline get(Item item){
        if (item instanceof BuildableItem) {
            if (item instanceof MultiBranchProject) {
                return new MultiBranchPipelineImpl((MultiBranchProject) item, getLink());
            } else if (item instanceof Job) {
                return new PipelineImpl((Job) item, getLink());
            }
        } else if (item instanceof ItemGroup) {
            return new PipelineFolderImpl((ItemGroup) item, getLink());
        }

        // TODO: I'm going to turn this into a decorator annotation
        throw new ServiceException.NotFoundException(String.format("Pipeline %s not found", item.getName()));
    }

    @Override
    @SuppressWarnings("unchecked")
    public Iterator<BluePipeline> iterator() {
        return getPipelines(itemGroup.getItems());
    }

    protected static boolean isMultiBranchProjectJob(BuildableItem item){
        return item instanceof WorkflowJob && item.getParent() instanceof MultiBranchProject;
    }

    protected  Iterator<BluePipeline> getPipelines(Collection<? extends Item> items){
        List<BluePipeline> pipelines = new ArrayList<>();
        for (Item item : items) {
            if(item instanceof MultiBranchProject){
                pipelines.add(new MultiBranchPipelineImpl((MultiBranchProject) item, getLink()));
            }else if(item instanceof BuildableItem && !isMultiBranchProjectJob((BuildableItem) item)
                && item instanceof Job){
                pipelines.add(new PipelineImpl((Job) item, getLink()));
            }else if(item instanceof ItemGroup){
                pipelines.add(new PipelineFolderImpl((ItemGroup) item, getLink()));
            }
        }
        return pipelines.iterator();
    }
}
