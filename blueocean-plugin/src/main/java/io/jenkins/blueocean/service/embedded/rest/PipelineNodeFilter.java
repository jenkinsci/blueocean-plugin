package io.jenkins.blueocean.service.embedded.rest;

import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import io.jenkins.blueocean.rest.model.BluePipelineNode;
import org.jenkinsci.plugins.workflow.actions.ErrorAction;
import org.jenkinsci.plugins.workflow.actions.LabelAction;
import org.jenkinsci.plugins.workflow.actions.StageAction;
import org.jenkinsci.plugins.workflow.actions.ThreadNameAction;
import org.jenkinsci.plugins.workflow.graph.FlowNode;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;
import org.jenkinsci.plugins.workflow.support.visualization.table.FlowGraphTable;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Filters {@link FlowGraphTable} to BlueOcean specific model representing DAG like graph objects
 *
 * @author Vivek Pandey
 */
public class PipelineNodeFilter {

    private final List<FlowGraphTable.Row> rows;
    private final WorkflowRun run;

    /* one to many relation of parent to children nodes */
    private final Map<FlowNode, List<FlowNode>> childNodeMap = new LinkedHashMap<>();

    /* one to many relation of child to parent nodes */
    private final Map<FlowNode, List<FlowNode>> parentNodeMap = new LinkedHashMap<>();
    private final Map<FlowNode, List<ErrorAction>> errorNodes = new LinkedHashMap<>();


    public PipelineNodeFilter(WorkflowRun run) {
        this.run = run;
        FlowGraphTable nodeGraphTable = new FlowGraphTable(run.getExecution());
        nodeGraphTable.build();

        this.rows = nodeGraphTable.getRows();
        filter();
    }

    public Map<FlowNode, List<FlowNode>> getchildNodeMap(){
        return childNodeMap;
    }

    public List<BluePipelineNode> getPipelineNodes(){
        if(childNodeMap.isEmpty()){
            return Collections.emptyList();
        }
        List<BluePipelineNode> stages = new ArrayList<>();
        for(FlowNode node: childNodeMap.keySet()){
            if(errorNodes.get(node) != null){
                ErrorAction[] errorActions = Iterables.toArray(errorNodes.get(node), ErrorAction.class);
                stages.add(new PipelineNodeImpl(run, node, this, errorActions));
            }else{
                stages.add(new PipelineNodeImpl(run, node, this));
            }
        }
        return stages;
    }

    private @Nullable FlowNode getFirstParents(FlowNode child){
        List<FlowNode> parents = parentNodeMap.get(child);
        return parents.size() > 0 ? parents.get(0) : null;
    }

    public List<FlowNode> getChildren(FlowNode parent){
        return childNodeMap.get(parent);
    }

    public final Predicate<FlowNode> acceptable = new Predicate<FlowNode>() {
        @Override
        public boolean apply(@Nullable FlowNode input) {
            return isStage.apply(input) || isParallel.apply(input);
        }
    };

    public static final Predicate<FlowNode> isStage = new Predicate<FlowNode>() {
        @Override
        public boolean apply(@Nullable FlowNode input) {
            return input !=null && input.getAction(StageAction.class) != null;
        }
    };

    public static final  Predicate<FlowNode> isParallel = new Predicate<FlowNode>() {
        @Override
        public boolean apply(@Nullable FlowNode input) {
            return input !=null && input.getAction(LabelAction.class) != null &&
                input.getAction(ThreadNameAction.class) != null;
        }
    };

    /**
     * Identifies Pipeline nodes and parallel branches, identifies the edges connecting them
     *
     */
    private void filter(){
        FlowNode previous=null;
        for(int i=0; i< rows.size(); i++) {
            FlowGraphTable.Row row = rows.get(i);
            FlowNode flowNode = row.getNode();

            ErrorAction action = flowNode.getError();
            if(previous != null && action != null){
                putErrorAction(previous,action);
            }
            if (acceptable.apply(flowNode)) {
                getOrCreateChildNodeMap(flowNode);
                if(isStage.apply(flowNode)) {
                    if(previous == null){
                        previous = flowNode;
                        if(action != null){
                            putErrorAction(previous,action);
                        }
                    }else {
                        getOrCreateChildNodeMap(previous).add(flowNode);
                        getOrCreateParentNodeMap(flowNode).add(previous);
                        previous = flowNode;
                    }
                }else if(isParallel.apply(flowNode)){
                    List<FlowNode> parallels = new ArrayList<>();
                    parallels.add(flowNode);

                    //fast forward till you find another stage
                    FlowNode nextStage=null;
                    FlowNode prevParallelNode=flowNode;
                    for(int j=i+1; j<rows.size();j++){
                        FlowGraphTable.Row r = rows.get(j);
                        FlowNode n = r.getNode();
                        if(n.getError() != null){
                            putErrorAction(prevParallelNode,n.getError());
                            if(previous != null) {
                                putErrorAction(previous, n.getError());
                            }
                        }
                        if(isParallel.apply(n)){
                            prevParallelNode = n;
                            parallels.add(n);
                        }else if(isStage.apply(n)){
                            nextStage = n;
                            i=j;
                            break;
                        }
                    }

                    for(FlowNode f: parallels){
                        List<FlowNode> cn = getOrCreateChildNodeMap(f);
                        if(nextStage != null) {
                            cn.add(nextStage);
                            getOrCreateParentNodeMap(nextStage).add(f);
                        }
                        if(previous != null) {
                            getOrCreateChildNodeMap(previous).add(f);
                            getOrCreateParentNodeMap(f).add(previous);
                        }
                    }

                    if(nextStage != null){
                        if(childNodeMap.get(nextStage) == null){
                            childNodeMap.put(nextStage, new ArrayList<FlowNode>());
                        }
                    }

                    previous = nextStage;
                }
            }
        }
    }

    /**
     * Create a union of current pipeline nodes with the one from future. Term future indicates that
     * this list of nodes are either in the middle of processing or failed somewhere in middle and we are
     * projecting future nodes in the pipeline.
     *
     * Last element of this node is patched to point to the first node of given list. First node of given
     * list is indexed at thisNodeList.size().
     *
     * @param futureNodes list of FlowNodes from lastSuccessfulNodes
     * @return list of FlowNode that is union of current set of nodes and the given list of nodes. If futureNodes
     * are not bigger than this pipeline nodes then no union is performed.
     * @see PipelineNodeContainerImpl#PipelineNodeContainerImpl(WorkflowRun)
     */
    public List<BluePipelineNode> union(Map<FlowNode, List<FlowNode>> futureNodes){
        if(childNodeMap.size() < futureNodes.size()){

            // XXX: If the pipeline was modified since last successful run then
            // the union might represent invalid future nodes.
            List<FlowNode> nodes = ImmutableList.copyOf(childNodeMap.keySet());
            List<FlowNode> thatNodes = ImmutableList.copyOf(futureNodes.keySet());
            int currentNodeSize = nodes.size();
            for(int i = nodes.size();  i < futureNodes.size(); i++){
                InactiveFlowNodeWrapper n = new InactiveFlowNodeWrapper(thatNodes.get(i));

                // Add the last successful pipeline's first node to the edge of current node's last node
                if(i == currentNodeSize) {
                    FlowNode latestNode = nodes.get(currentNodeSize-1);
                    if(PipelineNodeFilter.isStage.apply(latestNode)){
                        getOrCreateChildNodeMap(latestNode).add(n);
                    }else if(isParallel.apply(latestNode)){
                        /**
                         * If its a parallel node, find all its siblings and add the next node as
                         * edge (if not already present)
                         */
                        //parallel node has at most one paraent
                        FlowNode parent = getFirstParents(latestNode);
                        if(parent != null){
                            List<FlowNode> children = getChildren(parent);
                            for(FlowNode c: children){
                                // Add next node to the parallel node's edge
                                if(PipelineNodeFilter.isParallel.apply(c)){
                                    getOrCreateChildNodeMap(c).add(n);
                                }
                            }
                        }
                    }
                }
                childNodeMap.put(n, futureNodes.get(n.inactiveNode));
            }
        }
        return getPipelineNodes();
    }

    private List<FlowNode> getOrCreateChildNodeMap(@Nonnull FlowNode p){
        List<FlowNode> nodes = childNodeMap.get(p);
        if(nodes == null){
            nodes = new ArrayList<>();
            childNodeMap.put(p, nodes);
        }
        return nodes;
    }

    private List<FlowNode> getOrCreateParentNodeMap(@Nonnull FlowNode c){
        List<FlowNode> nodes = parentNodeMap.get(c);
        if(nodes == null){
            nodes = new ArrayList<>();
            parentNodeMap.put(c, nodes);
        }
        return nodes;
    }


    private void putErrorAction(@Nonnull FlowNode node, @Nonnull ErrorAction action){
        List<ErrorAction> actions = errorNodes.get(node);
        if(actions == null){
            actions = new ArrayList<>();
            errorNodes.put(node, actions);
        }
        actions.add(action);
    }

    public static class InactiveFlowNodeWrapper extends FlowNode{

        private final FlowNode inactiveNode;

        public InactiveFlowNodeWrapper(FlowNode node){
            super(node.getExecution(),node.getId());
            this.inactiveNode = node;
        }

        @Override
        protected String getTypeDisplayName() {
            return PipelineNodeUtil.getDisplayName(inactiveNode);
        }
    }
}
