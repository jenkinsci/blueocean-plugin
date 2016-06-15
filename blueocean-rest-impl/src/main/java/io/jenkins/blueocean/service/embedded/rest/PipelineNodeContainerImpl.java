package io.jenkins.blueocean.service.embedded.rest;

import hudson.model.Result;
import io.jenkins.blueocean.commons.ServiceException;
import io.jenkins.blueocean.rest.hal.Link;
import io.jenkins.blueocean.rest.model.BluePipelineNode;
import io.jenkins.blueocean.rest.model.BluePipelineNodeContainer;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * @author Vivek Pandey
 */
public class PipelineNodeContainerImpl extends BluePipelineNodeContainer {
    private final WorkflowRun run;
    private final Map<String, BluePipelineNode> nodeMap = new HashMap<>();
    private final Link self;

    List<BluePipelineNode> nodes = new ArrayList<>();

    public PipelineNodeContainerImpl(WorkflowRun run, Link parentLink) {
        this.self = parentLink.rel("nodes");
        this.run = run;

        WorkflowJob job = run.getParent();

        PipelineNodeGraphBuilder graphBuilder = new PipelineNodeGraphBuilder(run);
        //If build either failed or is in progress then return union with last successful pipeline run
        if(job.getLastBuild() != null
            && job.getLastBuild().getResult() != Result.SUCCESS
            && job.getLastSuccessfulBuild() != null
            && !job.getLastSuccessfulBuild().getId().equals(job.getLastBuild().getId())){
            PipelineNodeGraphBuilder pastBuild = new PipelineNodeGraphBuilder(job.getLastSuccessfulBuild());
            this.nodes = graphBuilder.union(pastBuild,getLink());
        }else{
            this.nodes = graphBuilder.getPipelineNodes(getLink());
        }
        for(BluePipelineNode node: nodes){
            nodeMap.put(node.getId(), node);
        }
    }

    @Override
    public BluePipelineNode get(String name) {
        if(nodeMap.get(name) != null){
            return nodeMap.get(name);
        }
        throw new ServiceException.NotFoundException(String.format("Stage %s not found in pipeline %s.",
            name, run.getParent().getName()));
    }

    @Override
    public Iterator<BluePipelineNode> iterator() {
        return nodes.iterator();
    }

    @Override
    public Link getLink() {
        return self;
    }
}
