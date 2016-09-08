package io.jenkins.blueocean.rest.impl.pipeline;

import com.google.common.base.Predicate;
import io.jenkins.blueocean.rest.model.BlueRun;
import org.jenkinsci.plugins.workflow.actions.ErrorAction;
import org.jenkinsci.plugins.workflow.actions.LabelAction;
import org.jenkinsci.plugins.workflow.actions.LogAction;
import org.jenkinsci.plugins.workflow.actions.StageAction;
import org.jenkinsci.plugins.workflow.actions.ThreadNameAction;
import org.jenkinsci.plugins.workflow.cps.nodes.StepEndNode;
import org.jenkinsci.plugins.workflow.flow.FlowExecution;
import org.jenkinsci.plugins.workflow.graph.FlowNode;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;
import org.jenkinsci.plugins.workflow.steps.FlowInterruptedException;

import javax.annotation.Nullable;
import java.util.List;

/**
 * @author Vivek Pandey
 */
public class PipelineNodeUtil {

    public static BlueRun.BlueRunResult getStatus(ErrorAction errorAction){
        if(errorAction == null){
            return BlueRun.BlueRunResult.SUCCESS;
        }else{
            return getStatus(errorAction.getError());
        }
    }

    public static BlueRun.BlueRunResult getStatus(Throwable error){
        if(error instanceof FlowInterruptedException){
            return BlueRun.BlueRunResult.ABORTED;
        }else{
            return BlueRun.BlueRunResult.FAILURE;
        }
    }

    public static PipelineNodeGraphBuilder.NodeRunStatus getStatus(WorkflowRun run){
        FlowExecution execution = run.getExecution();
        BlueRun.BlueRunResult result;
        BlueRun.BlueRunState state;
        if (execution == null) {
            result = BlueRun.BlueRunResult.NOT_BUILT;
            state = BlueRun.BlueRunState.QUEUED;
        } else if (execution.getCauseOfFailure() != null) {
            result = getStatus(execution.getCauseOfFailure());
            state = BlueRun.BlueRunState.FINISHED;
        } else if (execution.isComplete() &&  run.getResult() != null) { //workaround for https://issues.jenkins-ci.org/browse/JENKINS-38049
            switch (run.getResult().toString()){
                case "SUCCESS":
                    result = BlueRun.BlueRunResult.SUCCESS;
                    break;
                case "ABORTED":
                    result = BlueRun.BlueRunResult.ABORTED;
                    break;
                case "FAILURE":
                    result = BlueRun.BlueRunResult.FAILURE;
                    break;
                default:
                    result = BlueRun.BlueRunResult.UNKNOWN;
            }
            state = BlueRun.BlueRunState.FINISHED;
        } else {
            result = BlueRun.BlueRunResult.UNKNOWN;
            state = BlueRun.BlueRunState.RUNNING;
        }
        return new PipelineNodeGraphBuilder.NodeRunStatus(result,state);
    }


    public static String getDisplayName(FlowNode node) {
        return node.getAction(ThreadNameAction.class) != null
            ? node.getAction(ThreadNameAction.class).getThreadName()
            : node.getDisplayName();
    }

    public static boolean isStage(FlowNode node){
        return node !=null && (node.getAction(StageAction.class) != null
            || (node.getAction(LabelAction.class) != null && node.getAction(ThreadNameAction.class) == null));

    }

    public static boolean isParallelBranch(FlowNode node){
        return node !=null && node.getAction(LabelAction.class) != null &&
            node.getAction(ThreadNameAction.class) != null;
    }

    public static Predicate<FlowNode> isLoggable = new Predicate<FlowNode>() {
        @Override
        public boolean apply(@Nullable FlowNode input) {
            if(input == null)
                return false;
            return input.getAction(LogAction.class) != null;
        }
    };


    public static boolean isNestedInParallel(List<FlowNode> sortedNodes, FlowNode node){
        FlowNode p = getClosestEnclosingParallelBranch(sortedNodes,node, node.getParents());
        return isInBlock(p, getStepEndNode(sortedNodes, p), node);
    }



    private static FlowNode getClosestEnclosingParallelBranch(List<FlowNode> sortedNodes, FlowNode node, List<FlowNode> parents){
        for(FlowNode n: parents){
            if(isParallelBranch(n)){
                if(isInBlock(n, getStepEndNode(sortedNodes, n), node)) {
                    return n;
                }
            }
            return getClosestEnclosingParallelBranch(sortedNodes, node, n.getParents());
        }
        return null;
    }

    public static FlowNode getStepEndNode(List<FlowNode> sortedNodes, FlowNode startNode){
        for(int i = sortedNodes.size() - 1; i >=0; i--){
            FlowNode n = sortedNodes.get(i);
            if(n instanceof StepEndNode){
                StepEndNode endNode = (StepEndNode) n;
                if(endNode.getStartNode().equals(startNode))
                    return endNode;
            }
        }
        return null;
    }

    public static boolean isInBlock(FlowNode startNode, FlowNode endNode, FlowNode c){
        return isChildOf(startNode, c) && isChildOf(c, endNode);
    }

    public static boolean isChildOf(FlowNode parent, FlowNode child){
        if(child == null){
            return false;
        }
        for(FlowNode p:child.getParents()){
            if(p.equals(parent)){
                return true;
            }
            return isChildOf(parent, p);
        }
        return false;
    }

    public static boolean isBranchNestedInBranch(FlowNode node){
        for(FlowNode n: node.getParents()){
            if(isParallelBranch(node)){
                return true;
            }
            isBranchNestedInBranch(n);
        }
        return false;
    }

}
