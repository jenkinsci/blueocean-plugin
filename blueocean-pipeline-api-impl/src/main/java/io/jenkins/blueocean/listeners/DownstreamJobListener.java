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

        String dbg = "onStarted: " + run; // TODO: RM

        for (BuildUpstreamNodeAction action : run.getActions(BuildUpstreamNodeAction.class)) {
            Run triggerRun = Run.fromExternalizableId(action.getUpstreamRunId());
            if (triggerRun instanceof WorkflowRun) {
                FlowExecution execution = ((WorkflowRun) triggerRun).getExecution();
                FlowNode node;

                dbg += "\n\t      trigger: " + triggerRun;


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
                dbg += "\n\t         node: " + node;

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

                dbg += "\n\tblueGraphNode: " + blueGraphNode;

                if (blueGraphNode == null) {
                    LOGGER.warning("Could not find a suitable parent node for node " + node);
                    continue;
                }

                Link link = LinkResolver.resolveLink(run);
                if (link != null) {
                    blueGraphNode.addAction(new NodeDownstreamBuildAction(link, description));
                    try {
                        blueGraphNode.save();
                    } catch (IOException e) {
                        LOGGER.severe("Could not persist node: " + blueGraphNode);
                        LOGGER.severe(e.toString());
                    }
                }
            }
        }

        System.out.println(dbg); // TODO: RM
    }

    // TODO: Docs
    private FlowNode findBlueGraphNode(FlowNode actionNode) {
        String dbg = "findBlueGraphNode for " + actionNode; // TODO: RM
        try {
            FlowNode closestStageStart = null; // Closest instance of StepStartNode that is also a stage
            List<FlowNode> searchNodes = actionNode.getParents();

            if (actionNode instanceof StepStartNode && PipelineNodeUtil.isStage(actionNode)) {
                closestStageStart = actionNode;
            }

            int i = 0;
            lookForParallel: while (searchNodes.size() > 0) {
                i++;
                ArrayList<FlowNode> nextParents = new ArrayList<>();

                dbg += "\n\tCheck parents loop " + i;

                dbg += "\n\t\tclosestStage is " + closestStageStart;

                for (FlowNode node : searchNodes) {
                    dbg += "\n\t\t\tnode " + node;
                    if (PipelineNodeUtil.isParallelBranch(node)) {
                        // If we find the beginning of a parallel, this is the right one
                        dbg += " >>>> isParallel!";
                        return node;
                    }
                    if (closestStageStart == null && node instanceof StepStartNode && PipelineNodeUtil.isStage(node)) {
                        dbg += " set closestStageStart";
                        closestStageStart = node;
                    }
                    if (node instanceof StepEndNode && closestStageStart != null) {
                        dbg += " is StepEndNode, so give up looking for parallel";
                        // Stop looking for a parallel, so we don't go too far back in the graph
                        break lookForParallel;
                    }
                    nextParents.addAll(node.getParents());
                }

                searchNodes = nextParents;
            }

            return closestStageStart; // If there's no paralell root, return the nearest stage
        } finally {
            System.out.println(dbg); // TODO: RM
        }
    }
}
