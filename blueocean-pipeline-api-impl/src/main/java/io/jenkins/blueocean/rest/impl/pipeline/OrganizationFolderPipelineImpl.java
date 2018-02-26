package io.jenkins.blueocean.rest.impl.pipeline;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import hudson.model.Item;
import hudson.model.Queue;
import io.jenkins.blueocean.commons.ServiceException.UnexpectedErrorException;
import io.jenkins.blueocean.rest.Navigable;
import io.jenkins.blueocean.rest.Reachable;
import io.jenkins.blueocean.rest.annotation.Capability;
import io.jenkins.blueocean.rest.factory.BluePipelineFactory;
import io.jenkins.blueocean.rest.hal.Link;
import io.jenkins.blueocean.rest.impl.pipeline.scm.ScmSourceImpl;
import io.jenkins.blueocean.rest.model.BlueActionProxy;
import io.jenkins.blueocean.rest.model.BlueFavorite;
import io.jenkins.blueocean.rest.model.BlueFavoriteAction;
import io.jenkins.blueocean.rest.model.BlueIcon;
import io.jenkins.blueocean.rest.model.BlueOrganization;
import io.jenkins.blueocean.rest.model.BlueOrganizationFolder;
import io.jenkins.blueocean.rest.model.BluePipelineContainer;
import io.jenkins.blueocean.rest.model.BluePipelineScm;
import io.jenkins.blueocean.rest.model.BlueQueueContainer;
import io.jenkins.blueocean.rest.model.BlueQueueItem;
import io.jenkins.blueocean.rest.model.BlueRun;
import io.jenkins.blueocean.rest.model.BlueRunContainer;
import io.jenkins.blueocean.rest.model.BlueScmSource;
import io.jenkins.blueocean.rest.model.BlueTrendContainer;
import io.jenkins.blueocean.rest.model.Resource;
import io.jenkins.blueocean.service.embedded.rest.PipelineFolderImpl;
import io.jenkins.blueocean.service.embedded.rest.QueueItemImpl;
import jenkins.branch.OrganizationFolder;
import jenkins.model.Jenkins;
import jenkins.scm.api.metadata.AvatarMetadataAction;
import org.jenkinsci.plugins.workflow.multibranch.WorkflowMultiBranchProject;
import org.kohsuke.stapler.Stapler;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;
import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.json.JsonBody;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static io.jenkins.blueocean.rest.model.KnownCapabilities.BLUE_SCM;
import static io.jenkins.blueocean.rest.model.KnownCapabilities.JENKINS_ORGANIZATION_FOLDER;

/**
 * BlueOcean abstraction of {@link OrganizationFolder}
 *
 * @author Vivek Pandey
 */
@Capability({JENKINS_ORGANIZATION_FOLDER, BLUE_SCM})
public abstract class OrganizationFolderPipelineImpl extends BlueOrganizationFolder {
    final OrganizationFolder folder;
    private final PipelineFolderImpl pipelineFolder;
    private final BlueOrganization organization;

    public OrganizationFolderPipelineImpl(BlueOrganization organization, OrganizationFolder folder, Link parent) {
        this.organization = organization;
        this.folder = folder;
        this.pipelineFolder = new PipelineFolderImpl(organization, folder, parent);
    }

    @Override
    public BlueIcon getIcon() {
        final AvatarMetadataAction action = folder.getAction(AvatarMetadataAction.class);
        return action != null ? new OrganizationIcon(action, getLink()) : null;
    }

    @Navigable
    public BluePipelineContainer getPipelines(){
        return new MultiBranchPipelineContainerImpl(organization, folder, this);
    }

    @Override
    public Integer getNumberOfFolders() {
        return pipelineFolder.getNumberOfFolders();
    }

    @Override
    public Integer getNumberOfPipelines() {
        return pipelineFolder.getNumberOfPipelines();
    }

    @Override
    @Navigable
    public BlueRunContainer getRuns() {
        return new OrganizationFolderRunContainerImpl(this, this);
    }

    @Override
    public Collection<BlueActionProxy> getActions() {
        return pipelineFolder.getActions();
    }

    @Override
    public String getOrganizationName() {
        return organization.getName();
    }

    @Nonnull
    @Override
    public BlueOrganization getOrganization() {
        return organization;
    }

    @Override
    public String getName() {
        return pipelineFolder.getName();
    }

    @Override
    public String getDisplayName() {
        return pipelineFolder.getDisplayName();
    }

    @Override
    public String getFullName() {
        return pipelineFolder.getFullName();
    }

    @Override
    public String getFullDisplayName() {
        return pipelineFolder.getFullDisplayName();
    }

    @Override
    @Exported(inline = true)
    public BlueRun getLatestRun() {
        return new OrganizationFolderRunContainerImpl(this, this).get(OrganizationFolderRunImpl.RUN_ID);
    }

    @Override
    public Iterable<String> getPipelineFolderNames() {
        return Iterables.transform(folder.getItems(), new Function<Item, String>() {
            @Override
            public String apply(@Nullable Item input) {
                if(input instanceof WorkflowMultiBranchProject){
                    return input.getName();
                }
                return null;
            }
        });
    }

    @Override
    public Link getLink() {
        return pipelineFolder.getLink();
    }

    /**
     * Certain SCM provider organization folder implementation might support filtered repo search, if thats the case this method
     * must be overridden by their implementations.
     */
    @Override
    public boolean isScanAllRepos() {
        return true;
    }

    @Override
    public BlueScmSource getScmSource() {
        return new ScmSourceImpl(folder);
    }

    //lower than PipelineFolderImpl.PipelineFactoryImpl so that it gets looked up first
    public abstract static class OrganizationFolderFactory extends BluePipelineFactory {

        protected abstract OrganizationFolderPipelineImpl getFolder(jenkins.branch.OrganizationFolder folder, Reachable parent, BlueOrganization organization);

        @Override
        public OrganizationFolderPipelineImpl getPipeline(Item item, Reachable parent, BlueOrganization organization) {
            if (item instanceof jenkins.branch.OrganizationFolder) {
                return getFolder( (jenkins.branch.OrganizationFolder)item, parent, organization);
            }
            return null;
        }

        @Override
        public Resource resolve(Item context, Reachable parent, Item target, BlueOrganization organization) {
            OrganizationFolderPipelineImpl folder = getPipeline(context, parent, organization);
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
    public BlueQueueContainer getQueue() {
        return new BlueQueueContainer() {
            @Override
            public BlueQueueItem get(String name) {
                for(Queue.Item item: Jenkins.getInstance().getQueue().getItems(folder)){
                    if(item.getId() == Long.parseLong(name)){
                        return new QueueItemImpl(organization, item, OrganizationFolderPipelineImpl.this, 1);
                    }
                }
                return null;
            }

            @Override
            public Link getLink() {
                return OrganizationFolderPipelineImpl.this.getLink().rel("queue");
            }

            @Override
            public Iterator<BlueQueueItem> iterator() {
                return new Iterator<BlueQueueItem>(){
                    Iterator<Queue.Item> it = Jenkins.getInstance().getQueue().getItems(folder).iterator();
                    @Override
                    public boolean hasNext() {
                        return it.hasNext();
                    }

                    @Override
                    public BlueQueueItem next() {
                        return new QueueItemImpl(organization, it.next(), OrganizationFolderPipelineImpl.this, 1);
                    }

                    @Override
                    public void remove() {
                        //noop
                    }
                };
            }
        };
    }

    @Override
    public List<Object> getParameters() {
        return null;
    }

    @Override
    public BlueFavorite favorite(@JsonBody BlueFavoriteAction favoriteAction) {
        return null;
    }

    @Override
    public Map<String, Boolean> getPermissions() {
        return null;
    }

    @Override
    public BluePipelineScm getScm() {
        return new ScmResourceImpl(folder, this);
    }

    protected OrganizationFolder getFolder() {
        return folder;
    }

    public static class OrganizationIcon extends BlueIcon {

        private final AvatarMetadataAction action;
        private final Link parent;

        public OrganizationIcon(AvatarMetadataAction action, Link parent) {
            this.action = action;
            this.parent = parent;
        }

        @Override
        public void getUrl() {
            StaplerRequest req = Stapler.getCurrentRequest();
            String s = req.getParameter("s");
            if (s == null) {
                s = Integer.toString(DEFAULT_ICON_SIZE);
            }
            StaplerResponse resp = Stapler.getCurrentResponse();
            try {
                resp.setHeader("Cache-Control", "max-age=" + TimeUnit.DAYS.toDays(7));
                resp.sendRedirect(action.getAvatarImageOf(s));
            } catch (IOException e) {
                throw new UnexpectedErrorException("Could not provide icon", e);
            }
        }

        @Override
        public Link getLink() {
            return parent.rel("icon");
        }
    }

    @Override
    public BlueTrendContainer getTrends() {
        return null;
    }
}
