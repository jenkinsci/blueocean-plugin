package io.jenkins.blueocean.service.embedded.rest;

import io.jenkins.blueocean.commons.ServiceException;
import io.jenkins.blueocean.rest.model.BluePipelineStep;
import io.jenkins.blueocean.rest.model.BluePipelineStepContainer;
import org.jenkinsci.plugins.workflow.cps.nodes.StepAtomNode;
import org.jenkinsci.plugins.workflow.graph.FlowNode;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @author Vivek Pandey
 */
public class PipelineStepContainerImpl extends BluePipelineStepContainer {
    private final FlowNode node;
    private final PipelineNodeGraphBuilder graphBuilder;
    public PipelineStepContainerImpl(FlowNode node, PipelineNodeGraphBuilder graphBuilder) {
        this.node = node;
        this.graphBuilder = graphBuilder;
    }

    @Override
    public BluePipelineStep get(String name) {
        FlowNode node = graphBuilder.getNodeById(name);
        if(node == null){
            throw new ServiceException.NotFoundException(String.format("Node %s is not found", name));
        }
        if(!(node instanceof StepAtomNode)){
            throw new ServiceException.BadRequestExpception(String.format("Node %s:%s is not a step node.", name, node.getDisplayName()));
        }
        return new PipelineStepImpl(node, graphBuilder);
    }

    @Override
    public Iterator<BluePipelineStep> iterator() {
        List<FlowNode> nodes = graphBuilder.getSteps(node);
        List<BluePipelineStep> pipelineSteps = new ArrayList<>();
        for(FlowNode node:nodes){
            pipelineSteps.add(new PipelineStepImpl(node, graphBuilder));
        }
        return pipelineSteps.iterator();
    }
}
