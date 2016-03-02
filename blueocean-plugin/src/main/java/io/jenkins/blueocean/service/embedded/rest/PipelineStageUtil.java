package io.jenkins.blueocean.service.embedded.rest;

import io.jenkins.blueocean.rest.model.BluePipelineNode;
import org.jenkinsci.plugins.workflow.actions.StageAction;
import org.jenkinsci.plugins.workflow.actions.TimingAction;
import org.jenkinsci.plugins.workflow.graph.FlowNode;
import org.jenkinsci.plugins.workflow.steps.FlowInterruptedException;

/**
 * @author Vivek Pandey
 */
public class PipelineStageUtil {
    public static boolean isStageNode(FlowNode node){
        return node.getAction(StageAction.class) != null;
    }

    private static long getStartTime(FlowNode node){
        TimingAction timingAction = node.getAction(TimingAction.class);
        if(timingAction != null){
            return timingAction.getStartTime();
        }
        return 0;
    }
    public static BluePipelineNode.Status getStatus(FlowNode node){
        if (node.getError() == null) {
            return BluePipelineNode.Status.SUCCESS;
        } else if(node.getError().getError() instanceof FlowInterruptedException){
            return BluePipelineNode.Status.ABORTED;
        } else{
            return BluePipelineNode.Status.FAILED;
        }
    }


}
