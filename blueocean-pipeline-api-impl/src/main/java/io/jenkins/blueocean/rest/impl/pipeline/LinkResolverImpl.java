package io.jenkins.blueocean.rest.impl.pipeline;

import hudson.Extension;
import hudson.model.Queue;
import hudson.model.Run;
import io.jenkins.blueocean.rest.factory.BluePipelineFactory;
import io.jenkins.blueocean.rest.hal.Link;
import io.jenkins.blueocean.rest.hal.LinkResolver;
import io.jenkins.blueocean.rest.model.BluePipeline;
import io.jenkins.blueocean.rest.model.BluePipelineNode;
import io.jenkins.blueocean.rest.model.BluePipelineStep;
import io.jenkins.blueocean.rest.model.BlueRun;
import io.jenkins.blueocean.rest.model.BlueRunContainer;
import io.jenkins.blueocean.rest.model.Resource;
import org.jenkinsci.plugins.workflow.cps.nodes.StepAtomNode;
import org.jenkinsci.plugins.workflow.graph.FlowNode;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * @author Vivek Pandey
 */
@Extension
public class LinkResolverImpl extends LinkResolver {

    private final Logger logger = LoggerFactory.getLogger(io.jenkins.blueocean.service.embedded.LinkResolverImpl.class);

    @Override
    public Link resolve(Object modelObject) {
        if (modelObject instanceof FlowNode) {
            FlowNode flowNode = (FlowNode) modelObject;
            BlueRun r = resolveFlowNodeRun(flowNode);
            if (PipelineNodeUtil.isParallelBranch(flowNode) || PipelineNodeUtil.isStage(flowNode)) { // its Node
                if (r != null) {
                    return r.getLink().rel("nodes/" + flowNode.getId());
                }
            } else if (flowNode instanceof StepAtomNode && !PipelineNodeUtil.isStage(flowNode)) {
                if (r != null) {
                    return r.getLink().rel("steps/" + flowNode.getId());
                }
            }
        }else if(modelObject instanceof BluePipelineNode || modelObject instanceof BluePipelineStep){
            return ((Resource) modelObject).getLink();
        }
        return null;
    }


    private BlueRun resolveFlowNodeRun(FlowNode flowNode) {
        try {
            Queue.Executable executable = flowNode.getExecution().getOwner().getExecutable();
            if (executable != null && executable instanceof WorkflowRun) {
                WorkflowRun run = (WorkflowRun) executable;
                return (BlueRun) resolveRun(run);
            }
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
            return null;

        }
        return null;
    }

    private Resource resolveRun(Run run){
        Resource resource = BluePipelineFactory.resolve(run.getParent());
        if(resource instanceof BluePipeline){
            BluePipeline pipeline = (BluePipeline) resource;
            BlueRunContainer blueRunContainer = pipeline.getRuns();
            return blueRunContainer == null ? null : blueRunContainer.get(run.getId());
        }
        return null;
    }
}
