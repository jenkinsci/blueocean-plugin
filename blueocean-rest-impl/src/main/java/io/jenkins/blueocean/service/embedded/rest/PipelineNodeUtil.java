package io.jenkins.blueocean.service.embedded.rest;

import com.google.common.base.Predicate;
import io.jenkins.blueocean.rest.model.BlueRun;
import org.jenkinsci.plugins.workflow.actions.ErrorAction;
import org.jenkinsci.plugins.workflow.actions.LabelAction;
import org.jenkinsci.plugins.workflow.actions.LogAction;
import org.jenkinsci.plugins.workflow.actions.StageAction;
import org.jenkinsci.plugins.workflow.actions.ThreadNameAction;
import org.jenkinsci.plugins.workflow.flow.FlowExecution;
import org.jenkinsci.plugins.workflow.graph.FlowNode;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;
import org.jenkinsci.plugins.workflow.steps.FlowInterruptedException;

import javax.annotation.Nullable;

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
        } else if (execution.isComplete()) {
            result = BlueRun.BlueRunResult.SUCCESS;
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
        return node !=null && node.getAction(StageAction.class) != null;

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


}
