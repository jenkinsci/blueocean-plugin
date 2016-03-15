package io.jenkins.blueocean.service.embedded.rest;

import io.jenkins.blueocean.commons.ServiceException;
import io.jenkins.blueocean.rest.model.BluePipelineNode;
import io.jenkins.blueocean.rest.model.BluePipelineNodeContainer;
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
    List<BluePipelineNode> nodes = new ArrayList<>();

    public PipelineNodeContainerImpl(WorkflowRun run) {
        this.run = run;
        
        PipelineNodeFilter stageVisitor = new PipelineNodeFilter(run);
        this.nodes = stageVisitor.getPipelineNodes();
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
}
