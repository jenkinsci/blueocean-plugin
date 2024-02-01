package io.jenkins.blueocean.listeners;

import hudson.Extension;
import hudson.model.Action;
import hudson.model.Actionable;
import hudson.model.InvisibleAction;
import hudson.model.Queue;
import hudson.model.Run;
import io.jenkins.blueocean.rest.Reachable;
import io.jenkins.blueocean.rest.hal.Link;
import io.jenkins.blueocean.rest.hal.LinkResolver;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import jenkins.model.TransientActionFactory;
import org.jenkinsci.plugins.workflow.actions.FlowNodeAction;
import org.jenkinsci.plugins.workflow.graph.FlowNode;
import org.jenkinsci.plugins.workflow.support.steps.build.DownstreamBuildAction;
import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.export.ExportedBean;

/**
 * Annotates a FlowNode to point to a downstream build triggered by said node. Applied by
 * io.jenkins.blueocean.listeners.DownstreamJobListener in blueocean-pipeline-api-impl
 */
@ExportedBean
public class NodeDownstreamBuildAction extends InvisibleAction implements FlowNodeAction, Reachable {
    private static final Logger LOGGER = Logger.getLogger(NodeDownstreamBuildAction.class.getName());

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

    @Exported
    public Link getLink() {
        return link;
    }

    @Exported
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

    @Extension
    public static class FactoryImpl extends TransientActionFactory<FlowNode> {

        @Override
        public Class<FlowNode> type() {
            return FlowNode.class;
        }

        @Override
        public Collection<? extends Action> createFor(FlowNode node) {
            try {
                Queue.Executable executable = node.getExecution().getOwner().getExecutable();
                if (executable instanceof Actionable) {
                    DownstreamBuildAction action = ((Actionable) executable).getAction(DownstreamBuildAction.class);
                    if (action != null) {
                        for (DownstreamBuildAction.DownstreamBuild downstreamBuild : action.getDownstreamBuilds()) {
                            if (downstreamBuild.getFlowNodeId().equals(node.getId())) {
                                Run<?, ?> downstream = downstreamBuild.getBuild();
                                if (downstream != null) {
                                    Link link = LinkResolver.resolveLink(downstream);
                                    String description = downstream.getDescription();
                                    if (description == null) {
                                        description = downstream.getFullDisplayName();
                                    }
                                    return Collections.singleton(new NodeDownstreamBuildAction(link, description));
                                }
                            }
                        }
                    }
                }
            } catch (IOException e) {
                LOGGER.log(Level.WARNING, null, e);
            }
            return Collections.emptySet();
        }
    }
}
