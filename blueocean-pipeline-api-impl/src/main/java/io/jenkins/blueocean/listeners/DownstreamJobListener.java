package io.jenkins.blueocean.listeners;

import hudson.Extension;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.model.listeners.RunListener;
import io.jenkins.blueocean.rest.impl.pipeline.PipelineNodeUtil;
import io.jenkins.blueocean.service.embedded.rest.NodeDownstreamBuildAction;
import org.jenkinsci.plugins.workflow.flow.FlowExecution;
import org.jenkinsci.plugins.workflow.graph.FlowNode;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;
import org.jenkinsci.plugins.workflow.support.steps.build.BuildUpstreamNodeAction;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
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

                System.out.println(" trigger node " + node + " -- " + node.getDisplayName()); // TODO: RM

//                    // Look up the nearest StepStartNode which is where we want to add our action so BO will find it
//                    node = findContainingGraphNode(node);
//
//                    if (node == null) {
//                        LOGGER.warning("Could not retrieve upstream graph node");
//                        continue;
//                    }
//
//                    System.out.println("containing graph node " + node + " -- " + node.getDisplayName()); // TODO: RM

                // Add an action on the triggerRun node pointing to the currently executing run
                String description = run.getDescription();
                if (description == null) {
                    description = run.getFullDisplayName();
                }
                node.addAction(new NodeDownstreamBuildAction(run.getExternalizableId(), description));
            }
        }

    }

//    // TODO: explain / refactor this hairy shit, or collect the info during the graph walk instead
//    private FlowNode findContainingGraphNode(FlowNode node) {
//        return findContainingGraphNode(Collections.singletonList(node), 1);
//    }
//
//    // TODO: explain / refactor this hairy shit, or collect the info during the graph walk instead
//    private FlowNode findContainingGraphNode(Collection<FlowNode> nodes, int searchDepth) {
//        if (searchDepth > 10) {
//            // Escape hatch for weird / cyclic graphs
//            LOGGER.warning("Hit recurse depth limit searching for containing graph node!!!");
//            return null;
//        }
//        HashSet<FlowNode> ancestors = new HashSet<>();
//        for (FlowNode node : nodes) {
//            if (PipelineNodeUtil.isStage(node) && PipelineNodeUtil.isParallelBranch(node)) {
//                return node;
//            }
//            ancestors.addAll(node.getParents());
//        }
//
//        return findContainingGraphNode(ancestors, searchDepth + 1);
//    }
}
