package io.jenkins.blueocean.rest.impl.pipeline;

import org.jenkinsci.plugins.workflow.graph.AtomNode;
import org.jenkinsci.plugins.workflow.graph.FlowNode;
import org.jenkinsci.plugins.workflow.pipelinegraphanalysis.TimingInfo;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author Vivek Pandey
 */
public class FlowNodeWrapper {
    public enum NodeType {STAGE, PARALLEL, STEP, UNKNWON}

    private final FlowNode node;
    private final NodeRunStatus status;
    private final TimingInfo timingInfo;
    public final List<String> edges = new ArrayList<>();
    public final List<FlowNodeWrapper> steps = new ArrayList<>();
    public final NodeType type;

    private List<FlowNodeWrapper> parents = new ArrayList<>();


    public FlowNodeWrapper(FlowNode node, NodeRunStatus status, TimingInfo timingInfo) {
        this.node = node;
        this.status = status;
        this.timingInfo = timingInfo;
        if(PipelineNodeUtil.isStage(node)){
            this.type = NodeType.STAGE;
        }else if(PipelineNodeUtil.isParallelBranch(node)){
            this.type = NodeType.PARALLEL;
        }else if(node instanceof AtomNode){
            this.type = NodeType.STEP;
        }else{
            this.type = NodeType.UNKNWON;
        }
    }

    public NodeRunStatus getStatus(){
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

    public void addParent(FlowNodeWrapper parent){
        parents.add(parent);
    }

    public void addParents(Collection<FlowNodeWrapper> parents){
        parents.addAll(parents);
    }

    public @Nullable FlowNodeWrapper getFirstParent(){
        return parents.size() > 0 ? parents.get(0): null;
    }

    public List<FlowNodeWrapper> getParents(){
        return parents;
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
