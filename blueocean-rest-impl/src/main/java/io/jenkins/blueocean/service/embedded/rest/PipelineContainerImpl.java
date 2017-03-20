package io.jenkins.blueocean.service.embedded.rest;

import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import hudson.model.Item;
import hudson.model.ItemGroup;
import hudson.model.TopLevelItem;
import hudson.security.FullControlOnceLoggedInAuthorizationStrategy;
import io.jenkins.blueocean.commons.ServiceException;
import io.jenkins.blueocean.rest.Reachable;
import io.jenkins.blueocean.rest.hal.Link;
import io.jenkins.blueocean.rest.model.BluePipeline;
import io.jenkins.blueocean.rest.model.BluePipelineContainer;
import jenkins.model.Jenkins;
import org.acegisecurity.AccessDeniedException;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * @author Vivek Pandey
 */
public class PipelineContainerImpl extends BluePipelineContainer {
    private final @Nonnull ItemGroup itemGroup;
    private final Link self;

    public PipelineContainerImpl() {
        this(Jenkins.getInstance(),null);
    }

    public PipelineContainerImpl(ItemGroup itemGroup) {
        this(itemGroup,null);
    }

    public PipelineContainerImpl(ItemGroup itemGroup, Reachable parent) {
        this.itemGroup = itemGroup instanceof Jenkins ? new PermissionFilteredItemGroup((Jenkins) itemGroup) : itemGroup;
        if(parent!=null){
            this.self = parent.getLink().rel("pipelines");
        }else{
            this.self = OrganizationImpl.INSTANCE.getLink().rel("pipelines");
        }
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

    /**
     * Jenkins.getItems will return all items regardless of {@link FullControlOnceLoggedInAuthorizationStrategy#isAllowAnonymousRead()}
     * This implementation filters out {@link Item}s that do not have {@link Item#READ} which maintains backward compatibility
     * for versions without a fix for SECURITY-380
     */
    static class PermissionFilteredItemGroup implements ItemGroup<TopLevelItem> {
        private final Jenkins jenkins;

        public PermissionFilteredItemGroup(Jenkins jenkins) {
            this.jenkins = jenkins;
        }

        @Override
        public TopLevelItem getItem(String name) throws AccessDeniedException {
            return jenkins.getItem(name);
        }

        @Override
        public Collection<TopLevelItem> getItems() {
            return Collections2.filter(this.jenkins.getItems(), new Predicate<TopLevelItem>() {
                @Override
                public boolean apply(TopLevelItem input) {
                    return input.hasPermission(Item.READ);
                }
            });
        }

        @Override
        public String getFullName() {
            throw new UnsupportedOperationException();
        }

        @Override
        public String getFullDisplayName() {
            throw new UnsupportedOperationException();
        }

        @Override
        public String getUrl() {
            throw new UnsupportedOperationException();
        }

        @Override
        public String getUrlChildPrefix() {
            throw new UnsupportedOperationException();
        }

        @Override
        public File getRootDirFor(TopLevelItem child) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void onRenamed(TopLevelItem item, String oldName, String newName) throws IOException {
            throw new UnsupportedOperationException();
        }

        @Override
        public void onDeleted(TopLevelItem item) throws IOException {
            throw new UnsupportedOperationException();
        }

        @Override
        public String getDisplayName() {
            throw new UnsupportedOperationException();
        }

        @Override
        public File getRootDir() {
            throw new UnsupportedOperationException();
        }

        @Override
        public void save() throws IOException {
            throw new UnsupportedOperationException();
        }
    }
}
