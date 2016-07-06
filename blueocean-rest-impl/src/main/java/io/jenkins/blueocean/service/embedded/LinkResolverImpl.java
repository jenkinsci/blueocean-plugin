package io.jenkins.blueocean.service.embedded;

import hudson.model.Item;
import hudson.model.ItemGroup;
import hudson.model.Job;
import hudson.model.Queue;
import hudson.model.Run;
import io.jenkins.blueocean.rest.hal.Link;
import io.jenkins.blueocean.rest.hal.LinkResolver;
import io.jenkins.blueocean.rest.model.BluePipeline;
import io.jenkins.blueocean.rest.model.BluePipelineNode;
import io.jenkins.blueocean.rest.model.BluePipelineStep;
import io.jenkins.blueocean.rest.model.BlueRun;
import io.jenkins.blueocean.rest.model.Resource;
import io.jenkins.blueocean.service.embedded.rest.BluePipelineFactory;
import io.jenkins.blueocean.service.embedded.rest.PipelineNodeUtil;
import org.jenkinsci.plugins.workflow.cps.nodes.StepAtomNode;
import org.jenkinsci.plugins.workflow.graph.FlowNode;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Implementation of {@link LinkResolver}
 *
 * @author Kohsuke Kawaguchi
 * @author Vivek Pandey
 */
public class LinkResolverImpl extends LinkResolver {

    private final Logger logger = LoggerFactory.getLogger(LinkResolverImpl.class);

    @Override
    public Link resolve(Object modelObject) {
        if (modelObject instanceof Job) {
            Resource resource =  resolveJob((Job)modelObject);
            if(resource != null){
                return resource.getLink();
            }
        }else if(modelObject instanceof Item && modelObject instanceof ItemGroup){
            Resource resource = resolveFolder((Item) modelObject);
            if(resource!=null){
                return resource.getLink();
            }
        }else if(modelObject instanceof Run){
            Run run = (Run) modelObject;
            Resource resource = resolveRun(run);
            if(resource != null){
                return resource.getLink();
            }
        }else if(modelObject instanceof FlowNode){
            FlowNode flowNode = (FlowNode) modelObject;
            BlueRun r = resolveFlowNodeRun(flowNode);
            if(PipelineNodeUtil.isParallelBranch(flowNode) || PipelineNodeUtil.isStage(flowNode)){ // its Node
                if(r != null){
                    BluePipelineNode node = r.getNodes().get(flowNode.getId());
                    if(node != null){
                        return node.getLink();
                    }
                }
            }else if(flowNode instanceof StepAtomNode && !PipelineNodeUtil.isStage(flowNode)) {
                if(r != null){
                    BluePipelineStep step = r.getSteps().get(flowNode.getId());
                    if(step != null){
                        return step.getLink();
                    }
                }
            }
        }
        return null;
    }

    private Resource resolveJob(Job job){
        return BluePipelineFactory.resolve(job);
    }

    private Resource resolveFolder(Item folder){
        return BluePipelineFactory.resolve(folder);
    }

    private Resource resolveRun(Run run){
        Resource resource = resolveJob(run.getParent());
        if(resource instanceof BluePipeline){
            BluePipeline pipeline = (BluePipeline) resource;
            return pipeline.getRuns().get(run.getId());
        }
        return null;
    }

    private BlueRun resolveFlowNodeRun(FlowNode flowNode){
        try {
            Queue.Executable executable = flowNode.getExecution().getOwner().getExecutable();
            if(executable!= null && executable instanceof WorkflowRun){
                WorkflowRun run = (WorkflowRun) executable;
                return (BlueRun) resolveRun(run);
            }
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
            return null;
        }
        return null;
    }

}
