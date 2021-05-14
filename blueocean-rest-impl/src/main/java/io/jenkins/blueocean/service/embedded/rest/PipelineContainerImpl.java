package io.jenkins.blueocean.service.embedded.rest;

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.model.Item;
import hudson.model.ItemGroup;
import io.jenkins.blueocean.commons.ServiceException;
import io.jenkins.blueocean.rest.Reachable;
import io.jenkins.blueocean.rest.factory.BluePipelineFactory;
import io.jenkins.blueocean.rest.hal.Link;
import io.jenkins.blueocean.rest.model.BlueOrganization;
import io.jenkins.blueocean.rest.model.BluePipeline;
import io.jenkins.blueocean.rest.model.BluePipelineContainer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * @author Vivek Pandey
 */
public class PipelineContainerImpl extends BluePipelineContainer {
    private final @NonNull ItemGroup itemGroup;
    private final Link self;

    public PipelineContainerImpl(BlueOrganization organization, ItemGroup itemGroup, Reachable parent) {
        super(organization);
        this.itemGroup = itemGroup;
        this.self = parent.getLink().rel("pipelines");
    }
    @Override
    public Link getLink() {
        return self;
    }

    @Override
    public BluePipeline get(String name) {
        Item item;
        item = itemGroup.getItem(name);

        if(item == null){
            throw new ServiceException.NotFoundException(String.format("Pipeline %s not found", name));
        }
        return BluePipelineFactory.getPipelineInstance(item, this);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Iterator<BluePipeline> iterator() {
        return getPipelines(itemGroup.getItems());
    }

    public  Iterator<BluePipeline> getPipelines(Collection<? extends Item> items){
        items = ContainerFilter.filter(items);
        List<BluePipeline> pipelines = new ArrayList<>();
        for (Item item : items) {
            BluePipeline pipeline  = BluePipelineFactory.getPipelineInstance(item, this);
            if(pipeline != null){
                pipelines.add(pipeline);
            }
        }
        return pipelines.iterator();
    }
}
