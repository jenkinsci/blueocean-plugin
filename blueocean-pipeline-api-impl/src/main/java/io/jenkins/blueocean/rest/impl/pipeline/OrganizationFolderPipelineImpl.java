package io.jenkins.blueocean.rest.impl.pipeline;

import hudson.model.Item;
import hudson.model.Queue;
import io.jenkins.blueocean.commons.ServiceException.UnexpectedErrorException;
import io.jenkins.blueocean.rest.Navigable;
import io.jenkins.blueocean.rest.Reachable;
import io.jenkins.blueocean.rest.hal.Link;
import io.jenkins.blueocean.rest.model.BlueIcon;
import io.jenkins.blueocean.rest.model.BluePipelineContainer;
import io.jenkins.blueocean.rest.model.BlueQueueContainer;
import io.jenkins.blueocean.rest.model.BlueQueueItem;
import io.jenkins.blueocean.rest.model.BlueRun;
import io.jenkins.blueocean.rest.model.BlueRunContainer;
import io.jenkins.blueocean.service.embedded.rest.PipelineFolderImpl;
import io.jenkins.blueocean.service.embedded.rest.QueueItemImpl;
import jenkins.branch.OrganizationFolder;
import jenkins.model.Jenkins;
import jenkins.scm.api.metadata.AvatarMetadataAction;
import org.kohsuke.stapler.Stapler;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;
import org.kohsuke.stapler.export.Exported;

import java.io.IOException;
import java.util.Iterator;
import java.util.concurrent.TimeUnit;

/**
 * BlueOcean abstraction of {@link OrganizationFolder}
 *
 * @author Vivek Pandey
 */
public class OrganizationFolderPipelineImpl extends PipelineFolderImpl {
    final OrganizationFolder folder;

    public OrganizationFolderPipelineImpl(OrganizationFolder folder, Link parent) {
        super(folder, parent);
        this.folder = folder;

    }

    @Override
    public BlueIcon getIcon() {
        final AvatarMetadataAction action = folder.getAction(AvatarMetadataAction.class);
        return action != null ? new OrganizationIcon(action, getLink()) : null;
    }

    @Navigable
    public BluePipelineContainer getPipelines(){
        return new MultiBranchPipelineContainerImpl(folder, this);
    }

    @Override
    @Navigable
    public BlueRunContainer getRuns() {
        return new OrganizationFolderRunContainerImpl(this, this);
    }

    @Override
    @Exported(inline = true)
    public BlueRun getLatestRun() {
        return new OrganizationFolderRunContainerImpl(this, this).get(OrganizationFolderRunImpl.RUN_ID);
    }


    public abstract static class OrganizationFolderFactory extends PipelineFolderImpl.PipelineFactoryImpl {

        protected abstract OrganizationFolderPipelineImpl getFolder(jenkins.branch.OrganizationFolder folder, Reachable parent);

        @Override
        public OrganizationFolderPipelineImpl getPipeline(Item item, Reachable parent) {
            if (item instanceof jenkins.branch.OrganizationFolder) {
                return getFolder((jenkins.branch.OrganizationFolder)item, parent);
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
                        return new QueueItemImpl(item, OrganizationFolderPipelineImpl.this, 1);
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
                        return new QueueItemImpl(it.next(), OrganizationFolderPipelineImpl.this, 1);
                    }

                    @Override
                    public void remove() {
                        //noop
                    }
                };
            }
        };
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
}
