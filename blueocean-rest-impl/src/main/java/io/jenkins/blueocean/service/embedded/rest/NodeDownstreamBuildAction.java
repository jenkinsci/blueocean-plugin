package io.jenkins.blueocean.service.embedded.rest;

import hudson.model.Action;
import hudson.model.InvisibleAction;
import io.jenkins.blueocean.rest.hal.Link;
import org.jenkinsci.plugins.workflow.actions.FlowNodeAction;
import org.jenkinsci.plugins.workflow.graph.FlowNode;
import org.kohsuke.stapler.export.ExportedBean;

import javax.annotation.CheckForNull;
import java.util.Objects;

/**
 * Annotates a FlowNode to point to a downstream build triggered by said node. Applied by
 * io.jenkins.blueocean.listeners.DownstreamJobListener in blueocean-pipeline-api-impl
 */
@ExportedBean
public class NodeDownstreamBuildAction implements FlowNodeAction, BluePipelineAction {

    private final Link link;
    private final String description;

    public NodeDownstreamBuildAction(Link link, String description) {
        this.link = link;
        this.description = description;
    }

    @Override
    public void onLoad(FlowNode flowNode) {
        // Don't care for now
    }

    public Link getLink() {
        return link;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        NodeDownstreamBuildAction that = (NodeDownstreamBuildAction) o;
        return Objects.equals(link, that.link) &&
            Objects.equals(description, that.description);
    }

    @Override
    public int hashCode() {
        return Objects.hash(link, description);
    }

    @CheckForNull
    @Override
    public String getIconFileName() {
        return null;
    }

    @CheckForNull
    @Override
    public String getDisplayName() {
        return null;
    }

    @CheckForNull
    @Override
    public String getUrlName() {
        return link.getHref();
    }
}
