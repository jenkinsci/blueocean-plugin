package io.jenkins.blueocean.rest.impl.pipeline;

import io.jenkins.blueocean.rest.hal.Link;
import io.jenkins.blueocean.rest.model.BluePipelineStep;
import io.jenkins.blueocean.rest.model.BluePipelineStepContainer;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @author Vivek Pandey
 */
public class PipelineStepContainerImpl extends BluePipelineStepContainer {
    private final FlowNodeWrapper node;
    private final Link self;

    public PipelineStepContainerImpl(FlowNodeWrapper node, Link parentLink) {
        this.self = parentLink.rel("steps");
        this.node = node;
    }

    @Override
    public BluePipelineStep get(String name) {
//        FlowNodeWrapper node = graphBuilder.visitor.getStep(name);
//        if(node == null){
//            throw new ServiceException.NotFoundException(String.format("Node %s is not found", name));
//        }
//        return new PipelineStepImpl(node, getLink());
        //XXX: Fixme
        return null;
    }

    @Override
    public Iterator<BluePipelineStep> iterator() {
        if(node!=null) {
            List<BluePipelineStep> pipelineSteps = new ArrayList<>();
            List<FlowNodeWrapper> nodes = node.steps;
            for (FlowNodeWrapper node : nodes) {
                pipelineSteps.add( new PipelineStepImpl(node,getLink()));
            }
            return pipelineSteps.iterator();
        }else{
            //XXXFixme
            return null;//graphBuilder.getAllSteps(getLink()).iterator();
        }
    }

    @Override
    public Link getLink() {
        return self;
    }
}
