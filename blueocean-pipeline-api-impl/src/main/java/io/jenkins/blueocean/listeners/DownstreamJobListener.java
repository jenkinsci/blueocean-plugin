package io.jenkins.blueocean.listeners;

import hudson.Extension;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.model.listeners.RunListener;
import io.jenkins.blueocean.rest.hal.Link;
import io.jenkins.blueocean.rest.hal.LinkResolver;
import io.jenkins.blueocean.rest.impl.pipeline.PipelineNodeUtil;
import io.jenkins.blueocean.service.embedded.rest.NodeDownstreamBuildAction;
import org.jenkinsci.plugins.workflow.cps.nodes.StepEndNode;
import org.jenkinsci.plugins.workflow.cps.nodes.StepStartNode;
import org.jenkinsci.plugins.workflow.flow.FlowExecution;
import org.jenkinsci.plugins.workflow.graph.FlowNode;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;
import org.jenkinsci.plugins.workflow.support.steps.build.BuildUpstreamNodeAction;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Listens to creation of jobs that are triggered by an upstream job, much like BuildTriggerListener from
 * pipeline-build-step. Unlike BuildTriggerListener, here we just add an action to the upstream job for later retrieval
 * rather than injecting information into its build log, in order to be a little more flexible at the cost of a bit more
 * work later to retrieve the details.
 */
@Extension
public class DownstreamJobListener extends RunListener<Run<?, ?>> {

    private static final Logger LOGGER = Logger.getLogger(DownstreamJobListener.class.getName());

    @Override
    public void onStarted(Run<?, ?> run, TaskListener listener) {

        for (BuildUpstreamNodeAction action : run.getActions(BuildUpstreamNodeAction.class)) {
            Run triggerRun = Run.fromExternalizableId(action.getUpstreamRunId());
            if (triggerRun instanceof WorkflowRun) {
                FlowExecution execution = ((WorkflowRun) triggerRun).getExecution();
                FlowNode node;

                if (execution == null) {
                    LOGGER.warning("Could not retrieve upstream FlowExecution");
                    continue;
                }

                try {
                    node = execution.getNode(action.getUpstreamNodeId());
                } catch (IOException e) {
                    LOGGER.warning("Could not retrieve upstream node: " + e);
                    continue;
                }

                if (node == null) {
                    LOGGER.warning("Could not retrieve upstream node (null)");
                    continue;
                }

                // Add an action on the triggerRun node pointing to the currently executing run
                String description = run.getDescription();
                if (description == null) {
                    description = run.getFullDisplayName();
                }

                // Find the node that will be used to create the cut-down graph for visualisation
                FlowNode blueGraphNode = findBlueGraphNode(node);

                if (blueGraphNode == null) {
                    LOGGER.warning("Could not find a suitable parent node for node " + node);
                    continue;
                }

                Link link = LinkResolver.resolveLink(run);
                if (link != null) {
                    try {
                           // Add to the node we'll use for the visualisation
                        blueGraphNode.addAction(new NodeDownstreamBuildAction(link, description));
                        blueGraphNode.save();

                        if (!blueGraphNode.equals(node)) {
                            // Also add to the actual trigger node so we can find it later by step
                            node.addAction(new NodeDownstreamBuildAction(link, description));
                            node.save();
                        }

                    } catch (IOException e) {
                        LOGGER.severe("Could not persist node: " + e);
                    }
                }
            }
        }
    }

    // TODO: Docs
    private FlowNode findBlueGraphNode(FlowNode actionNode) {
        FlowNode closestStageStart = null; // Closest instance of StepStartNode that is also a stage
        List<FlowNode> searchNodes = actionNode.getParents();

        if (actionNode instanceof StepStartNode && PipelineNodeUtil.isStage(actionNode)) {
            closestStageStart = actionNode;
        }

        while (searchNodes.size() > 0) {
            ArrayList<FlowNode> nextParents = new ArrayList<>();

            for (FlowNode node : searchNodes) {
                if (PipelineNodeUtil.isParallelBranch(node)) {
                    // If we find the beginning of a parallel, this is the right one
                    return node;
                }
                if (closestStageStart == null && node instanceof StepStartNode && PipelineNodeUtil.isStage(node)) {
                    closestStageStart = node;
                }
                if (node instanceof StepEndNode && closestStageStart != null) {
                    // Stop looking for a parallel, so we don't go too far back in the graph
                    return closestStageStart;
                }
                nextParents.addAll(node.getParents());
            }

            searchNodes = nextParents;
        }

        return closestStageStart; // In case this is a non-parallel first stage
    }
}
