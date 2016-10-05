package io.jenkins.blueocean.rest.impl.pipeline;

import io.jenkins.blueocean.rest.hal.Link;
import io.jenkins.blueocean.rest.model.BlueActionProxy;
import io.jenkins.blueocean.rest.model.BluePipelineNode;
import io.jenkins.blueocean.rest.model.BluePipelineStep;
import io.jenkins.blueocean.rest.model.BluePipelineStepContainer;
import io.jenkins.blueocean.rest.model.BlueRun;
import org.jenkinsci.plugins.workflow.graph.FlowNode;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

/**
 * Implementation of {@link BluePipelineNode}.
 *
 * @author Vivek Pandey
 * @see FlowNode
 */
public class PipelineNodeImpl extends BluePipelineNode {
    private final FlowNodeWrapper node;
    private final List<Edge> edges;
    private final Long durationInMillis;
    private final PipelineNodeGraphBuilder.NodeRunStatus status;
    private final Link self;
    private final WorkflowRun run;

    public PipelineNodeImpl(FlowNodeWrapper node, Link parentLink, WorkflowRun run) {
        this.node = node;
        this.run = run;
        this.edges = buildEdges(node.edges);
        this.status = node.getStatus();
        this.durationInMillis = node.getTiming().getTotalDurationMillis();
        this.self = parentLink.rel(node.getId());
    }

    @Override
    public String getId() {
        return node.getId();
    }

    @Override
    public String getDisplayName() {
        return PipelineNodeUtil.getDisplayName(node.getNode());
    }

    @Override
    public BlueRun.BlueRunResult getResult() {
        if(isInactiveNode()){
            return null;
        }
        return status.getResult();
    }

    @Override
    public BlueRun.BlueRunState getStateObj() {
        if(isInactiveNode()){
            return null;
        }
        return status.getState();
    }

    @Override
    public Date getStartTime() {
        if(isInactiveNode()){
            return null;
        }
        long nodeTime = node.getTiming().getStartTimeMillis();
        return new Date(nodeTime);
    }

    @Override
    public List<Edge> getEdges() {
        return edges;
    }

    @Override
    public Long getDurationInMillis() {
        return durationInMillis;
    }

    /**
     * No logs for Node as Node by itself doesn't have any log to repot, its steps inside it that has logs
     *
     * @see BluePipelineStep#getLog()
     */
    @Override
    public Object getLog() {
        return null;
    }

    @Override
    public BluePipelineStepContainer getSteps() {
        return new PipelineStepContainerImpl(node, self, run);
    }

    @Override
    public Link getLink() {
        return self;
    }

    @Override
    public Collection<BlueActionProxy> getActions() {
        return PipelineImpl.getActionProxies(node.getNode().getAllActions(), this);
    }


    public static class EdgeImpl extends Edge{
        private final String id;

        public EdgeImpl(String id) {
            this.id = id;
        }

        @Override
        public String getId() {
            return id;
        }
    }

    private List<Edge> buildEdges(List<String> nodes){
        List<Edge> edges  = new ArrayList<>();
        if(!nodes.isEmpty()) {
            for (String id:nodes) {
                edges.add(new EdgeImpl(id));
            }
        }
        return edges;
    }

    private boolean isInactiveNode(){
        return node.getNode() instanceof PipelineNodeGraphBuilder.InactiveFlowNodeWrapper;
    }
}
