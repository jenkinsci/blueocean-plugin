package io.jenkins.blueocean.rest.impl.pipeline;

import io.jenkins.blueocean.rest.hal.Link;
import io.jenkins.blueocean.rest.model.BluePipelineStep;
import io.jenkins.blueocean.rest.model.BluePipelineStepContainer;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;

import java.util.Iterator;

/**
 * @author Vivek Pandey
 */
public class PipelineStepContainerImpl extends BluePipelineStepContainer {
    private final FlowNodeWrapper node;
    private final Link self;
    private final WorkflowRun run;


    public PipelineStepContainerImpl(FlowNodeWrapper node, Link parentLink, WorkflowRun run) {
        this.self = parentLink.rel("steps");
        this.node = node;
        this.run = run;

    }

    public PipelineStepContainerImpl(WorkflowRun run, Link parentLink) {
        this.self = parentLink.rel("steps");
        this.node = null;
        this.run = run;
    }

    @Override
    public BluePipelineStep get(String name) {
        NodeGraphBuilder builder = NodeGraphBuilder.NodeGraphBuilderFactory.getInstance(run);
        return  builder.getPipelineNodeStep(name, getLink());
    }

    @Override
    public Iterator<BluePipelineStep> iterator() {
        NodeGraphBuilder builder = NodeGraphBuilder.NodeGraphBuilderFactory.getInstance(run);
        return (node == null)
            ? builder.getPipelineNodeSteps(getLink()).iterator()
            : builder.getPipelineNodeSteps(node.getId(), getLink()).iterator();
    }

    @Override
    public Link getLink() {
        return self;
    }
}
