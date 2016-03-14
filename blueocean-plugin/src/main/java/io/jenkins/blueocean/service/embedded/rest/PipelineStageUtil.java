package io.jenkins.blueocean.service.embedded.rest;

import hudson.model.Result;
import io.jenkins.blueocean.rest.model.BlueRun;
import org.jenkinsci.plugins.workflow.graph.FlowNode;
import org.jenkinsci.plugins.workflow.steps.FlowInterruptedException;

/**
 * @author Vivek Pandey
 */
public class PipelineStageUtil {

    public static BlueRun.BlueRunResult getStatus(FlowNode node){
        if (node.getError() == null) {
            return BlueRun.BlueRunResult.SUCCESS;
        } else if(node.getError().getError() instanceof FlowInterruptedException){
            Result result = ((FlowInterruptedException)node.getError().getError()).getResult();
            return BlueRun.BlueRunResult.valueOf(result.toString());
        } else{
            return BlueRun.BlueRunResult.FAILURE;
        }
    }


}
