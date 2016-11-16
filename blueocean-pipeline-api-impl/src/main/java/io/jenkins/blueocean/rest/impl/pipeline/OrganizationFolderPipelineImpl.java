package io.jenkins.blueocean.rest.impl.pipeline;

import hudson.Extension;
import hudson.model.Item;
import hudson.model.Queue;
import io.jenkins.blueocean.rest.Navigable;
import io.jenkins.blueocean.rest.Reachable;
import io.jenkins.blueocean.rest.hal.Link;
import io.jenkins.blueocean.rest.model.BluePipelineContainer;
import io.jenkins.blueocean.rest.model.BlueQueueContainer;
import io.jenkins.blueocean.rest.model.BlueQueueItem;
import io.jenkins.blueocean.rest.model.BlueRun;
import io.jenkins.blueocean.rest.model.BlueRunContainer;
import io.jenkins.blueocean.rest.model.Resource;
import io.jenkins.blueocean.service.embedded.rest.BluePipelineFactory;
import io.jenkins.blueocean.service.embedded.rest.PipelineFolderImpl;
import io.jenkins.blueocean.service.embedded.rest.QueueItemImpl;
import jenkins.branch.OrganizationFolder;
import jenkins.model.Jenkins;
import org.kohsuke.stapler.export.Exported;

import java.util.Iterator;

/**
 * @author Vivek Pandey
 */
public class OrganizationFolderPipelineImpl extends PipelineFolderImpl {
    final OrganizationFolder folder;

    public OrganizationFolderPipelineImpl(OrganizationFolder folder, Link parent) {
        super(folder, parent);
        this.folder = folder;

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

    @Extension(ordinal = 0)
    public static class PipelineFactoryImpl extends BluePipelineFactory {

        public OrganizationFolderPipelineImpl getPipeline(Item item, Reachable parent) {
            return item instanceof OrganizationFolder ? new OrganizationFolderPipelineImpl((OrganizationFolder) item,
                    parent.getLink()) : null;
        }

        public Resource resolve(Item context, Reachable parent, Item target) {
            OrganizationFolderPipelineImpl folder = this.getPipeline(context, parent);
            if (folder != null) {
                if (context == target) {
                    return folder;
                }
                Item nextChild = findNextStep(folder.folder, target);
                for (BluePipelineFactory f : all()) {
                    Resource answer = f.resolve(nextChild, folder, target);
                    if (answer != null)
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
}
