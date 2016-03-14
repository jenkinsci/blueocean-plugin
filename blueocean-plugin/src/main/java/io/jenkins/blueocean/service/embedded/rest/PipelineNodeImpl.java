package io.jenkins.blueocean.service.embedded.rest;

import io.jenkins.blueocean.rest.model.BluePipelineNode;
import io.jenkins.blueocean.rest.model.BlueRun;
import org.jenkinsci.plugins.workflow.actions.ThreadNameAction;
import org.jenkinsci.plugins.workflow.actions.TimingAction;
import org.jenkinsci.plugins.workflow.graph.FlowNode;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Implementation of {@link BluePipelineNode}.
 *
 * @author Vivek Pandey
 * @see FlowNode
 */
public class PipelineNodeImpl extends BluePipelineNode {
    private final FlowNode node;
    private final List<Edge> edges;

    public PipelineNodeImpl(final FlowNode stage, List<FlowNode> children) {
        this.node = stage;
        if(children.isEmpty()){
            this.edges = null;
            return;
        }
        this.edges = new ArrayList<>();
        for(final FlowNode c : children){
            this.edges.add(new EdgeImpl(node,c));
        }
    }

    @Override
    public String getId() {
        return node.getId();
    }

    @Override
    public String getDisplayName() {
        return node.getAction(ThreadNameAction.class) != null
            ? node.getAction(ThreadNameAction.class).getThreadName()
            : node.getDisplayName();
    }

    @Override
    public BlueRun.BlueRunResult getResult() {
        return PipelineStageUtil.getStatus(node);
    }

    @Override
    public Date getStartTime() {
        long nodeTime = TimingAction.getStartTime(node);
        return new Date(nodeTime);
    }

    @Override
    public List<Edge> getEdges() {
        return edges;
    }

    private static class EdgeImpl extends Edge{
        private final FlowNode node;
        private final FlowNode edge;

        public EdgeImpl(FlowNode node, FlowNode edge) {
            this.node = node;
            this.edge = edge;
        }

        @Override
        public String getId() {
            return edge.getId();
        }

        @Override
        public long getDurationInMillis() {
            TimingAction t = node.getAction(TimingAction.class);
            TimingAction c = edge.getAction(TimingAction.class);
            if(t!= null){
                if(c != null){
                    return c.getStartTime() - t.getStartTime();
                }
            }
            return -1;
        }
    }
}
