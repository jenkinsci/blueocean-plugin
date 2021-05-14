package io.jenkins.blueocean.service.embedded.rest;

import com.google.common.collect.Iterables;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import hudson.Extension;
import hudson.model.AbstractItem;
import hudson.model.Item;
import hudson.model.ItemGroup;
import io.jenkins.blueocean.commons.ServiceException;
import io.jenkins.blueocean.rest.Reachable;
import io.jenkins.blueocean.rest.factory.BluePipelineFactory;
import io.jenkins.blueocean.rest.hal.Link;
import io.jenkins.blueocean.rest.model.BlueActionProxy;
import io.jenkins.blueocean.rest.model.BlueFavorite;
import io.jenkins.blueocean.rest.model.BlueFavoriteAction;
import io.jenkins.blueocean.rest.model.BlueIcon;
import io.jenkins.blueocean.rest.model.BlueOrganization;
import io.jenkins.blueocean.rest.model.BluePipeline;
import io.jenkins.blueocean.rest.model.BluePipelineContainer;
import io.jenkins.blueocean.rest.model.BluePipelineFolder;
import io.jenkins.blueocean.rest.model.BluePipelineScm;
import io.jenkins.blueocean.rest.model.BlueTrendContainer;
import io.jenkins.blueocean.rest.model.Resource;
import io.jenkins.blueocean.service.embedded.util.Disabler;
import org.kohsuke.stapler.json.JsonBody;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * @author Vivek Pandey
 */
public class PipelineFolderImpl extends BluePipelineFolder {
    protected final BlueOrganization organization;
    private final ItemGroup folder;
    protected final Link parent;

    public PipelineFolderImpl(BlueOrganization organization, ItemGroup folder, Link parent) {
        this.organization = organization;
        this.folder = folder;
        this.parent = parent;
    }

    @Override
    public String getOrganizationName() {
        return organization.getName();
    }

    @NonNull
    @Override
    public BlueOrganization getOrganization() {
        return organization;
    }

    @Override
    public String getName() {
        if(folder instanceof AbstractItem)
            return ((AbstractItem) folder).getName();
        else
            return folder.getDisplayName();
    }

    @Override
    public String getDisplayName() {
        return folder.getDisplayName();
    }

    @Override
    public String getFullName() {
        if (folder instanceof Item) {
            return AbstractPipelineImpl.getFullName(organization, (Item) folder);
        } else {
            return null;
        }
    }

    @Override
    public String getFullDisplayName() {
        if (folder instanceof Item) {
            return AbstractPipelineImpl.getFullDisplayName(organization, (Item) folder);
        } else {
            return folder.getDisplayName();
        }
    }

    @Override
    public Collection<BlueActionProxy> getActions() {
        return Collections.emptyList();
    }

    @Override
    public List<Object> getParameters() {
        return null;
    }

    @Override
    public BluePipelineContainer getPipelines() {
        return new PipelineContainerImpl(organization, folder, this);
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
    public BlueFavorite favorite(@JsonBody BlueFavoriteAction favoriteAction) {
        throw new ServiceException.MethodNotAllowedException("Cannot favorite a folder");
    }

    @Override
    public Map<String, Boolean> getPermissions() {
        if(folder instanceof AbstractItem){
            AbstractItem item = (AbstractItem) folder;
            return AbstractPipelineImpl.getPermissions(item);
        }else{
            return null;
        }

    }

    @Override
    public BluePipelineScm getScm() {
        return null;
    }

    @Override
    public Link getLink() {
        return organization.getLink().rel("pipelines").rel(AbstractPipelineImpl.getRecursivePathFromFullName(this));
    }

    @Extension(ordinal = -10)
    public static class PipelineFactoryImpl extends BluePipelineFactory {

        @Override
        public PipelineFolderImpl getPipeline(Item item, Reachable parent, BlueOrganization organization) {
            if (item instanceof ItemGroup) {
                return new PipelineFolderImpl(organization, (ItemGroup) item, parent.getLink());
            }
            return null;
        }

        @Override
        public Resource resolve(Item context, Reachable parent, Item target, BlueOrganization organization) {
            PipelineFolderImpl folder = getPipeline(context, parent, organization);
            if (folder!=null) {
                if(context == target){
                    return folder;
                }
                Item nextChild = findNextStep(folder.folder,target);
                for (BluePipelineFactory f : all()) {
                    Resource answer = f.resolve(nextChild, folder, target, organization);
                    if (answer!=null)
                        return answer;
                }
            }
            return null;
        }
    }

    @Override
    public BlueIcon getIcon() {
        return null;
    }

    @Override
    @SuppressFBWarnings(value = "RCN_REDUNDANT_NULLCHECK_OF_NONNULL_VALUE", justification = "getPipelines() can definitely be null see MatrixProjectImpl so findbugs is wrong...")
    public Iterable<String> getPipelineFolderNames() {
        BluePipelineContainer bluePipelineContainer = getPipelines();
        if(bluePipelineContainer==null) {
            return Collections.emptyList();
        }
        return Iterables.transform(bluePipelineContainer, input -> {
            if (input != null && input instanceof BluePipelineFolder) {
                return input.getName();
            }
            return null;
        });
    }

    @Override
    public BlueTrendContainer getTrends() {
        return null;
    }

    @Override
    @SuppressFBWarnings(value = "NP_BOOLEAN_RETURN_NULL", justification = "isDisabled will return null if the job type doesn't support it")
    public Boolean getDisabled() {
        return Disabler.isDisabled(folder);
    }

    @Override
    public void enable() throws IOException {
        if (getPermissions().getOrDefault(BluePipeline.CONFIGURE_PERMISSION, Boolean.FALSE)) {
            Disabler.makeDisabled(folder, false);
        }
    }

    @Override
    public void disable() throws IOException {
        if (getPermissions().getOrDefault(BluePipeline.CONFIGURE_PERMISSION, Boolean.FALSE)) {
            Disabler.makeDisabled(folder, true);
        }
    }
}
