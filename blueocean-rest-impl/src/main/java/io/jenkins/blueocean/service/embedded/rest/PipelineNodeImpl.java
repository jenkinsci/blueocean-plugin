package io.jenkins.blueocean.service.embedded.rest;

import hudson.console.AnnotatedLargeText;
import io.jenkins.blueocean.rest.model.BluePipelineNode;
import io.jenkins.blueocean.rest.model.BluePipelineStepContainer;
import io.jenkins.blueocean.rest.model.BlueRun;
import org.jenkinsci.plugins.workflow.actions.LogAction;
import org.jenkinsci.plugins.workflow.actions.TimingAction;
import org.jenkinsci.plugins.workflow.graph.FlowNode;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;
import org.jenkinsci.plugins.workflow.support.visualization.table.FlowGraphTable;

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
    private final List<FlowNode> children;
    private final List<Edge> edges;
    private final WorkflowRun run;
    private final Long durationInMillis;
    private final PipelineNodeGraphBuilder.NodeRunStatus status;
    private final PipelineNodeGraphBuilder nodeGraphBuilder;

    public PipelineNodeImpl(WorkflowRun run, final FlowNode node, PipelineNodeGraphBuilder.NodeRunStatus status, PipelineNodeGraphBuilder nodeGraphBuilder) {
        this.run = run;
        this.node = node;
        this.children = nodeGraphBuilder.getChildren(node);
        this.edges = buildEdges();
        this.status = status;
        if(getStateObj() == BlueRun.BlueRunState.FINISHED){
            this.durationInMillis = nodeGraphBuilder.getDurationInMillis(node);
        }else if(getStateObj() == BlueRun.BlueRunState.RUNNING){
            this.durationInMillis = System.currentTimeMillis()-TimingAction.getStartTime(node);
        }else{
            this.durationInMillis = null;
        }
        this.nodeGraphBuilder = nodeGraphBuilder;
    }

    @Override
    public String getId() {
        return node.getId();
    }

    @Override
    public String getDisplayName() {
        return PipelineNodeUtil.getDisplayName(node);
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
        long nodeTime = TimingAction.getStartTime(node);
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

    @Override
    public Object getLog() {
        FlowGraphTable nodeGraphTable = new FlowGraphTable(run.getExecution());
        nodeGraphTable.build();

        List<FlowGraphTable.Row> rows = nodeGraphTable.getRows();

        List<AnnotatedLargeText> logs = new ArrayList<>();

        for(int i=0; i<rows.size(); i++){
            FlowGraphTable.Row row = rows.get(i);
            if(row.getNode().equals(node)){
                if(PipelineNodeUtil.isLoggable.apply(row.getNode())){
                    logs.add(row.getNode().getAction(LogAction.class).getLogText());
                }

                for(int j=i+1; j < rows.size(); j++){
                    FlowGraphTable.Row subStepRow = rows.get(j);
                    // if it's stage collect all nodes till next stage is encountered
                    // Or if it's parallel, then wait till next parallel branch is encountered
                    if(PipelineNodeUtil.isStage(node) &&
                        PipelineNodeUtil.isStage(subStepRow.getNode())
                        ||
                        PipelineNodeUtil.isParallelBranch(node) &&
                            (PipelineNodeUtil.isParallelBranch(subStepRow.getNode()) || PipelineNodeUtil.isStage(subStepRow.getNode()))
                        ){
                        break;
                    }else{
                        if(PipelineNodeUtil.isLoggable.apply(subStepRow.getNode())){
                            logs.add(subStepRow.getNode().getAction(LogAction.class).getLogText());
                        }

                    }
                }
                break;
            }
        }

        return new LogResource(logs.toArray(new AnnotatedLargeText[logs.size()]));
    }

    @Override
    public BluePipelineStepContainer getSteps() {
        return new PipelineStepContainerImpl(node, nodeGraphBuilder);
    }


    public static class EdgeImpl extends Edge{
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
    }

    private List<Edge> buildEdges(){
        List<Edge> edges  = new ArrayList<>();
        if(!this.children.isEmpty()) {
            for (final FlowNode c : children) {
                edges.add(new EdgeImpl(node, c));
            }
        }
        return edges;
    }

    private boolean isInactiveNode(){
        return node instanceof PipelineNodeGraphBuilder.InactiveFlowNodeWrapper;
    }
}
