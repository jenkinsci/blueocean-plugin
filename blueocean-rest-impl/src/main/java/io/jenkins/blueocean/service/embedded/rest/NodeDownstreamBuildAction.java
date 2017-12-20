package io.jenkins.blueocean.service.embedded.rest;

import hudson.model.InvisibleAction;
import hudson.model.Run;
import org.jenkinsci.plugins.workflow.actions.FlowNodeAction;
import org.jenkinsci.plugins.workflow.graph.FlowNode;

import java.util.Objects;

/**
 * Annotates a FlowNode to point to a downstream build triggered by said node. Applied by
 * io.jenkins.blueocean.listeners.DownstreamJobListener in blueocean-pipeline-api-impl
 */
public class NodeDownstreamBuildAction extends InvisibleAction implements FlowNodeAction, BluePipelineAction {

    private final String runExternalizableId;
    private final String description;

    public NodeDownstreamBuildAction(String runExternalizableId, String description) {
        this.runExternalizableId = runExternalizableId;
        this.description = description;
    }

    @Override
    public void onLoad(FlowNode flowNode) {
        // Don't care for now
    }

    public String getRunExternalizableId() {
        return runExternalizableId;
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
        return Objects.equals(runExternalizableId, that.runExternalizableId) &&
            Objects.equals(description, that.description);
    }

    @Override
    public int hashCode() {
        return Objects.hash(runExternalizableId, description);
    }
}
