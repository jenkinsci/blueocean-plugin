package io.jenkins.blueocean.rest.impl.pipeline;

import hudson.model.Item;
import io.jenkins.blueocean.rest.Reachable;
import io.jenkins.blueocean.rest.hal.Link;
import io.jenkins.blueocean.service.embedded.rest.PipelineFolderImpl;
import org.kohsuke.stapler.export.Exported;

public abstract class OrganizationFolder extends PipelineFolderImpl {

    protected final jenkins.branch.OrganizationFolder folder;

    public OrganizationFolder(jenkins.branch.OrganizationFolder folder, Link parent) {
        super(folder, parent);
        this.folder = folder;
    }

    @Exported
    public String getIcon() {
        return folder.getIcon().getImageOf("32x32");
    }

    public abstract static class OrganizationFolderFactory extends PipelineFactoryImpl {

        protected abstract OrganizationFolder getFolder(jenkins.branch.OrganizationFolder folder, Reachable parent);

        @Override
        public OrganizationFolder getPipeline(Item item, Reachable parent) {
            if (item instanceof jenkins.branch.OrganizationFolder) {
                return getFolder((jenkins.branch.OrganizationFolder)item, parent);
            }
            return null;
        }
    }
}
