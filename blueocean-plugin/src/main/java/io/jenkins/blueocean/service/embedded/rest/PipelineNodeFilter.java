package io.jenkins.blueocean.service.embedded.rest;

import com.google.common.base.Predicate;
import io.jenkins.blueocean.rest.model.BluePipelineNode;
import org.jenkinsci.plugins.workflow.actions.LabelAction;
import org.jenkinsci.plugins.workflow.actions.StageAction;
import org.jenkinsci.plugins.workflow.actions.ThreadNameAction;
import org.jenkinsci.plugins.workflow.graph.FlowNode;
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

    public PipelineNodeFilter(List<FlowGraphTable.Row> rows) {
        this.rows = rows;
    }

    public List<BluePipelineNode> getPipelineNodes(){
        filter();
        if(nodeMap.isEmpty()){
            return Collections.emptyList();
        }
        List<BluePipelineNode> stages = new ArrayList<>();
        for(FlowNode node: nodeMap.keySet()){
            stages.add(new PipelineNodeImpl(node, nodeMap.get(node)));
        }
        return stages;
    }

    private final Predicate<FlowNode> acceptable = new Predicate<FlowNode>() {
        @Override
        public boolean apply(@Nullable FlowNode input) {
            return isStage.apply(input) || isParallel.apply(input);
        }
    };

    private final Predicate<FlowNode> isStage = new Predicate<FlowNode>() {
        @Override
        public boolean apply(@Nullable FlowNode input) {
            return input !=null && input.getAction(StageAction.class) != null;
        }
    };

    private final  Predicate<FlowNode> isParallel = new Predicate<FlowNode>() {
        @Override
        public boolean apply(@Nullable FlowNode input) {
            return input !=null && input.getAction(LabelAction.class) != null &&
                input.getAction(ThreadNameAction.class) != null;
        }
    };

    private final Map<FlowNode, List<FlowNode>> nodeMap = new LinkedHashMap<>();

    /**
     * Identifies Pipeline nodes and parallel branches, identifies the edges connecting them
     *
     */
    private void filter(){
        FlowNode previous=null;
        for(int i=0; i< rows.size(); i++) {
            FlowGraphTable.Row row = rows.get(i);
            FlowNode flowNode = row.getNode();
            if (acceptable.apply(flowNode)) {

                getOrCreate(flowNode);

                if(isStage.apply(flowNode)) {
                    if(previous == null){
                        previous = flowNode;
                    }else {
                        nodeMap.get(previous).add(flowNode);
                        previous = flowNode;
                    }
                }else if(isParallel.apply(flowNode)){
                    List<FlowNode> parallels = new ArrayList<>();
                    parallels.add(flowNode);

                    //fast forward till you find another stage
                    FlowNode nextStage=null;
                    for(int j=i+1; j<rows.size();j++){
                        FlowGraphTable.Row r = rows.get(j);
                        if(isParallel.apply(r.getNode())){
                            parallels.add(r.getNode());
                        }else if(isStage.apply(r.getNode())){
                            nextStage = r.getNode();
                            if(nodeMap.get(nextStage) == null){
                                nodeMap.put(nextStage, new ArrayList<FlowNode>());
                            }
                            i=j;
                            break;
                        }
                    }

                    for(FlowNode f: parallels){
                        List<FlowNode> cn = getOrCreate(f);
                        if(nextStage != null) {
                            cn.add(nextStage);
                        }
                        if(previous != null) {
                            getOrCreate(previous).add(f);
                        }
                    }

                    previous = nextStage;
                }
            }
        }
    }

    private List<FlowNode> getOrCreate(@Nonnull FlowNode p){
        List<FlowNode> nodes = nodeMap.get(p);
        if(nodes == null){
            nodes = new ArrayList<>();
            nodeMap.put(p, nodes);
        }
        return nodes;
    }

}
