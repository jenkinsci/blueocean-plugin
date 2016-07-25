package io.jenkins.blueocean.rest.impl.pipeline;

import io.jenkins.blueocean.commons.ServiceException;
import io.jenkins.blueocean.rest.hal.Link;
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
    private final Link self;

    public PipelineStepContainerImpl(FlowNode node, PipelineNodeGraphBuilder graphBuilder, Link parentLink) {
        this.self = parentLink.rel("steps");
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
        return new PipelineStepImpl(node, graphBuilder, getLink());
    }

    @Override
    public Iterator<BluePipelineStep> iterator() {
        List<BluePipelineStep> pipelineSteps = new ArrayList<>();
        if(node!=null) {
            List<FlowNode> nodes = graphBuilder.getSteps(node);
            for (FlowNode node : nodes) {
                pipelineSteps.add(new PipelineStepImpl(node, graphBuilder, getLink()));
            }
        }else{
            List<FlowNode> nodes = graphBuilder.getAllSteps();
            for(FlowNode node:nodes){
                pipelineSteps.add(new PipelineStepImpl(node, graphBuilder, getLink()));
            }
        }
        return pipelineSteps.iterator();
    }

    @Override
    public Link getLink() {
        return self;
    }
}
