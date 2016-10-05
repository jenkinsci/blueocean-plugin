package io.jenkins.blueocean.rest.impl.pipeline;

import org.jenkinsci.plugins.workflow.graph.FlowNode;
import org.jenkinsci.plugins.workflow.pipelinegraphanalysis.TimingInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Vivek Pandey
 */
public class FlowNodeWrapper {
    private final FlowNode node;
    private final PipelineNodeGraphBuilder.NodeRunStatus status;
    private final TimingInfo timingInfo;
    public final List<String> edges = new ArrayList<>();
    public final List<FlowNodeWrapper> steps = new ArrayList<>();


    public FlowNodeWrapper(FlowNode node, PipelineNodeGraphBuilder.NodeRunStatus status, TimingInfo timingInfo) {
        this.node = node;
        this.status = status;
        this.timingInfo = timingInfo;
    }

    public PipelineNodeGraphBuilder.NodeRunStatus getStatus(){
        return status;
    }

    public TimingInfo getTiming(){
        return timingInfo;
    }

    public String getId(){
        return node.getId();
    }

    public FlowNode getNode(){
        return node;
    }

    public void addEdge(String id){
        this.edges.add(id);
    }

    public void addEdges(List<String> edges){
        this.edges.addAll(edges);
    }

    @Override
    public boolean equals(Object obj) {
        if(!(obj instanceof FlowNodeWrapper)){
            return false;
        }
        return node.equals(obj);
    }

    public FlowNode getFlowNode(){
        return node;
    }


    @Override
    public int hashCode() {
        return node.hashCode();
    }
}
