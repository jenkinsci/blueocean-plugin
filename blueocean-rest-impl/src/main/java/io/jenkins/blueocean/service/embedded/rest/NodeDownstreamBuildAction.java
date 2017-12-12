package io.jenkins.blueocean.service.embedded.rest;

import hudson.model.InvisibleAction;
import org.jenkinsci.plugins.workflow.actions.FlowNodeAction;
import org.jenkinsci.plugins.workflow.graph.FlowNode;

import java.util.Objects;

// TODO: Docs, clean up
public class NodeDownstreamBuildAction extends InvisibleAction implements FlowNodeAction {

    private final String runExternalizableId;

    public NodeDownstreamBuildAction(String runExternalizableId) {
        this.runExternalizableId = runExternalizableId;
    }

    @Override
    public void onLoad(FlowNode flowNode) {
        // Don't care for now
    }

    public String getRunExternalizableId() {
        return runExternalizableId;
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
        return Objects.equals(runExternalizableId, that.runExternalizableId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(runExternalizableId);
    }
}
