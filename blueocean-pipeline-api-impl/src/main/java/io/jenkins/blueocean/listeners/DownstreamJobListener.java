package io.jenkins.blueocean.listeners;

import hudson.Extension;
import hudson.model.Cause;
import hudson.model.CauseAction;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.model.listeners.RunListener;
import io.jenkins.blueocean.service.embedded.DownstreamJobAction;
import io.jenkins.blueocean.service.embedded.rest.NodeDownstreamBuildAction;
import org.jenkinsci.plugins.workflow.cps.nodes.StepStartNode;
import org.jenkinsci.plugins.workflow.flow.FlowExecution;
import org.jenkinsci.plugins.workflow.graph.FlowNode;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;
import org.jenkinsci.plugins.workflow.support.steps.build.BuildUpstreamNodeAction;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
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
            System.out.println("run " + run + " started from node " + action.getUpstreamNodeId() + " in run " + action.getUpstreamRunId()); // TODO: RM
            Run triggerRun = Run.fromExternalizableId(action.getUpstreamRunId());
            if (triggerRun instanceof WorkflowRun) {
                FlowExecution execution = ((WorkflowRun) triggerRun).getExecution();
                FlowNode node;
                try {
                    node = execution.getNode(action.getUpstreamNodeId());
                } catch (IOException e) {
                    LOGGER.warning("Could not retrieve upstream node: " + e);
                    continue;
                }

                System.out.println("!!!!!    Found upstream node " + node); // TODO: RM

                // Look up the nearest StepStartNode which is where we want to add our action so BO will find it
                node = findContainingStepStartNode(node);

                System.out.println("!!!!! upstream StepStartNode " + node); // TODO: RM

                if (node == null) {
                    LOGGER.warning("Could not retrieve upstream StepStartNode");
                    continue;
                }


                // Add an action on the triggerRun node pointing to the currently executing run
                node.addAction(new NodeDownstreamBuildAction(run.getExternalizableId()));
            }
        }

    }

    // TODO: explain this hairy shit, or collect the info during the graph walk instead
    private StepStartNode findContainingStepStartNode(FlowNode node) {
        return findContainingStepStartNode(Collections.singletonList(node), 1);
    }

    // TODO: explain this hairy shit, or collect the info during the graph walk instead
    private StepStartNode findContainingStepStartNode(Collection<FlowNode> nodes, int searchDepth) {
        if (searchDepth > 10) {
            // Escape hatch for weird / cyclic graphs
            LOGGER.warning("Hit recurse depth limit searching for containing StepStartNode!!!");
            return null;
        }
        ArrayList<FlowNode> ancestors = new ArrayList<>();
        for (FlowNode node : nodes) {
            if (node instanceof StepStartNode) {
                return (StepStartNode) node;
            }
            ancestors.addAll(node.getParents());
        }

        return findContainingStepStartNode(ancestors, searchDepth +1);
    }
}
