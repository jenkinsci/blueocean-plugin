package io.jenkins.blueocean.service.embedded.rest;

import hudson.Extension;
import hudson.model.BuildableItem;
import hudson.model.Item;
import hudson.model.ItemGroup;
import hudson.model.Job;
import hudson.model.TopLevelItem;
import io.jenkins.blueocean.commons.ServiceException;
import io.jenkins.blueocean.rest.model.BluePipeline;
import io.jenkins.blueocean.rest.model.BluePipelineContainer;
import jenkins.branch.MultiBranchProject;
import jenkins.model.Jenkins;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * @author Vivek Pandey
 */
@Extension
public class PipelineContainerImpl extends BluePipelineContainer {
    private final ItemGroup itemGroup;

    public PipelineContainerImpl(ItemGroup itemGroup) {
        this.itemGroup = itemGroup;
    }

    public PipelineContainerImpl() {
        this.itemGroup = null;
    }

    @Override
    public BluePipeline get(String name) {
        Item item;
        if(itemGroup == null){
            item = Jenkins.getActiveInstance().getItemByFullName(name);
        }else{
            item = itemGroup.getItem(name);
        }

        if(item == null){
            throw new ServiceException.NotFoundException(String.format("Pipeline %s not found", name));
        }

        if (item instanceof BuildableItem) {
            if (item instanceof MultiBranchProject) {
                return new MultiBranchPipelineImpl((MultiBranchProject) item);
            } else if (item instanceof Job) {
                return new PipelineImpl((Job) item);
            }
        } else if (item instanceof ItemGroup) {
            return new PipelineFolderImpl((ItemGroup) item);
        }

        // TODO: I'm going to turn this into a decorator annotation
        throw new ServiceException.NotFoundException(String.format("Pipeline %s not found", name));
    }

    @Override
    @SuppressWarnings("unchecked")
    public Iterator<BluePipeline> iterator() {
        if(itemGroup != null){
            return getPipelines(itemGroup.getItems());
        }else{
            return getPipelines(Jenkins.getActiveInstance().getItems(TopLevelItem.class));
        }
    }

    protected static boolean isMultiBranchProjectJob(BuildableItem item){
        return item instanceof WorkflowJob && item.getParent() instanceof MultiBranchProject;
    }

    protected static Iterator<BluePipeline> getPipelines(Collection<? extends Item> items){
        List<BluePipeline> pipelines = new ArrayList<>();
        for (Item item : items) {
            if(item instanceof MultiBranchProject){
                pipelines.add(new MultiBranchPipelineImpl((MultiBranchProject) item));
            }else if(item instanceof BuildableItem && !isMultiBranchProjectJob((BuildableItem) item)
                && item instanceof Job){
                pipelines.add(new PipelineImpl((Job) item));
            }else if(item instanceof ItemGroup){
                pipelines.add(new PipelineFolderImpl((ItemGroup) item));
            }
        }
        return pipelines.iterator();
    }
}
