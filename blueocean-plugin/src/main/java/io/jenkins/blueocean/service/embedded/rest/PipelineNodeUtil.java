package io.jenkins.blueocean.service.embedded.rest;

import io.jenkins.blueocean.rest.model.BlueRun;
import org.jenkinsci.plugins.workflow.actions.ErrorAction;
import org.jenkinsci.plugins.workflow.actions.ThreadNameAction;
import org.jenkinsci.plugins.workflow.graph.FlowNode;
import org.jenkinsci.plugins.workflow.steps.FlowInterruptedException;

/**
 * @author Vivek Pandey
 */
public class PipelineNodeUtil {

    public static BlueRun.BlueRunResult getStatus(FlowNode node, ErrorAction errorAction){
        if(errorAction == null || errorAction.getError() == null){
            if(node.getExecution().isComplete()){
                return BlueRun.BlueRunResult.SUCCESS;
            }else{
                return BlueRun.BlueRunResult.UNKNOWN;
            }
        }
        if(errorAction.getError()  != null && errorAction.getError() instanceof FlowInterruptedException){
            return BlueRun.BlueRunResult.ABORTED;
        }else{
            return BlueRun.BlueRunResult.FAILURE;
        }
    }

    public static BlueRun.BlueRunState getState(FlowNode node){
        if(node.isRunning()){
            return BlueRun.BlueRunState.RUNNING;
        }else if(node.getExecution().isComplete()){
            return BlueRun.BlueRunState.FINISHED;
        }else{
            return BlueRun.BlueRunState.QUEUED;
        }
    }
    public static String getDisplayName(FlowNode node) {
        return node.getAction(ThreadNameAction.class) != null
            ? node.getAction(ThreadNameAction.class).getThreadName()
            : node.getDisplayName();
    }
}
