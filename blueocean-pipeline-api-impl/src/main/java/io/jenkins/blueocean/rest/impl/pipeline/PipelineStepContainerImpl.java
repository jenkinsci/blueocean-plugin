package io.jenkins.blueocean.rest.impl.pipeline;

import io.jenkins.blueocean.rest.hal.Link;
import io.jenkins.blueocean.rest.model.BluePipelineStep;
import io.jenkins.blueocean.rest.model.BluePipelineStepContainer;

import java.util.Iterator;

/**
 * @author Vivek Pandey
 */
public class PipelineStepContainerImpl extends BluePipelineStepContainer {
    private final FlowNodeWrapper node;
    private final Link self;
    private final String runExternalizableId;


    public PipelineStepContainerImpl(FlowNodeWrapper node, Link parentLink, String runExternalizableId) {
        this.self = parentLink.rel("steps");
        this.node = node;
        this.runExternalizableId = runExternalizableId;

    }

    public PipelineStepContainerImpl(String runExternalizableId, Link parentLink) {
        this.self = parentLink.rel("steps");
        this.node = null;
        this.runExternalizableId = runExternalizableId;
    }

    @Override
    public BluePipelineStep get(String name) {
        NodeGraphBuilder builder = NodeGraphBuilder.NodeGraphBuilderFactory.getInstance(PipelineRunImpl.findRun(runExternalizableId));
        return  builder.getPipelineNodeStep(name, getLink());
    }

    @Override
    public Iterator<BluePipelineStep> iterator() {
        NodeGraphBuilder builder = NodeGraphBuilder.NodeGraphBuilderFactory.getInstance(PipelineRunImpl.findRun(runExternalizableId));
        return (node == null)
            ? builder.getPipelineNodeSteps(getLink()).iterator()
            : builder.getPipelineNodeSteps(node.getId(), getLink()).iterator();
    }

    @Override
    public Link getLink() {
        return self;
    }
}
