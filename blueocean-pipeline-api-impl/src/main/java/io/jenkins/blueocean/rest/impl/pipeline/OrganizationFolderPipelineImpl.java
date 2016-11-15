package io.jenkins.blueocean.rest.impl.pipeline;

import hudson.Extension;
import hudson.model.Item;
import io.jenkins.blueocean.rest.Navigable;
import io.jenkins.blueocean.rest.Reachable;
import io.jenkins.blueocean.rest.hal.Link;
import io.jenkins.blueocean.rest.model.BluePipelineContainer;
import io.jenkins.blueocean.rest.model.BlueRun;
import io.jenkins.blueocean.rest.model.BlueRunContainer;
import io.jenkins.blueocean.rest.model.Resource;
import io.jenkins.blueocean.service.embedded.rest.BluePipelineFactory;
import io.jenkins.blueocean.service.embedded.rest.PipelineFolderImpl;
import jenkins.branch.OrganizationFolder;
import org.kohsuke.stapler.export.Exported;

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
}
