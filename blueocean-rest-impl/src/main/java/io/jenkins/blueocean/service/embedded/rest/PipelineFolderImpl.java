package io.jenkins.blueocean.service.embedded.rest;

import hudson.Extension;
import hudson.model.Item;
import hudson.model.ItemGroup;
import io.jenkins.blueocean.commons.ServiceException;
import io.jenkins.blueocean.rest.Reachable;
import io.jenkins.blueocean.rest.hal.Link;
import io.jenkins.blueocean.rest.model.BlueActionProxy;
import io.jenkins.blueocean.rest.model.BluePipeline;
import io.jenkins.blueocean.rest.model.BluePipelineContainer;
import io.jenkins.blueocean.rest.model.BluePipelineFactory;
import io.jenkins.blueocean.rest.model.BluePipelineFolder;
import io.jenkins.blueocean.service.embedded.util.FavoriteUtil;
import org.kohsuke.stapler.json.JsonBody;

import java.util.Collection;
import java.util.Collections;

import static io.jenkins.blueocean.service.embedded.rest.PipelineImpl.getRecursivePathFromFullName;

/**
 * @author Vivek Pandey
 */
public class PipelineFolderImpl extends BluePipelineFolder {

    private final ItemGroup folder;
    private final Link parent;

    public PipelineFolderImpl(ItemGroup folder, Link parent) {
        this.folder = folder;
        this.parent = parent;
    }

    @Override
    public String getOrganization() {
        return OrganizationImpl.INSTANCE.getName();
    }

    @Override
    public String getName() {
        return folder.getDisplayName();
    }

    @Override
    public String getDisplayName() {
        return folder.getDisplayName();
    }

    @Override
    public String getFullName() {
        return folder.getFullName();
    }

    @Override
    public Collection<BlueActionProxy> getActions() {
        return Collections.emptyList();
    }

    @Override
    public BluePipelineContainer getPipelines() {
        return new PipelineContainerImpl(folder, this);
    }

    @Override
    public Integer getNumberOfFolders() {
        int count=0;
        for(BluePipeline p:getPipelines ()){
            if(p instanceof BluePipelineFolder){
                count++;
            }
        }
        return count;
    }

    @Override
    public Integer getNumberOfPipelines() {
        int count=0;
        for(BluePipeline p:getPipelines ()){
            if(!(p instanceof BluePipelineFolder)){
                count++;
            }
        }
        return count;
    }


    @Override
    public void favorite(@JsonBody FavoriteAction favoriteAction) {
        if(favoriteAction == null) {
            throw new ServiceException.BadRequestExpception("Must provide pipeline name");
        }

        FavoriteUtil.favoriteJob(folder.getFullName(), favoriteAction.isFavorite());
    }

    @Override
    public Link getLink() {
        return OrganizationImpl.INSTANCE.getLink().rel("pipelines").rel(getRecursivePathFromFullName(this));
    }

    @Extension(ordinal = 0)
    public static class PipelineFactoryImpl extends BluePipelineFactory{

        @Override
        public BluePipeline getPipeline(Item item, Reachable parent) {
            if (item instanceof ItemGroup) {
                return new PipelineFolderImpl((ItemGroup) item, parent.getLink());
            }
            return null;
        }
    }
}
