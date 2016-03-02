package io.jenkins.blueocean.service.embedded.rest;

import io.jenkins.blueocean.rest.model.BluePipelineNode;
import org.jenkinsci.plugins.workflow.actions.ThreadNameAction;
import org.jenkinsci.plugins.workflow.actions.TimingAction;
import org.jenkinsci.plugins.workflow.graph.FlowNode;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author Vivek Pandey
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
        for(final FlowNode n : children){
            this.edges.add(new Edge(){
                @Override
                public String getId() {
                    return n.getId();
                }

                @Override
                public long getDurationInMillis() {
                    TimingAction t = stage.getAction(TimingAction.class);
                    TimingAction c = n.getAction(TimingAction.class);
                    if(t!= null){
                        if(c != null){
                            return c.getStartTime() - t.getStartTime();
                        }
                    }
                    return -1;
                }
            });
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
    public Status getStatus() {
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
}
