package io.jenkins.blueocean.rest.impl.pipeline;

import com.google.common.base.Predicate;
import io.jenkins.blueocean.rest.hal.Link;
import io.jenkins.blueocean.rest.model.BluePipelineNode;
import io.jenkins.blueocean.rest.model.BluePipelineStep;
import io.jenkins.blueocean.rest.model.BlueRun;
import org.jenkinsci.plugins.workflow.actions.NotExecutedNodeAction;
import org.jenkinsci.plugins.workflow.cps.nodes.StepEndNode;
import org.jenkinsci.plugins.workflow.graph.FlowNode;
import org.jenkinsci.plugins.workflow.graphanalysis.DepthFirstScanner;
import org.jenkinsci.plugins.workflow.graphanalysis.ForkScanner;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;
import org.jenkinsci.plugins.workflow.pipelinegraphanalysis.GenericStatus;
import org.jenkinsci.plugins.workflow.pipelinegraphanalysis.StageChunkFinder;
import org.jenkinsci.plugins.workflow.pipelinegraphanalysis.TimingInfo;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Vivek Pandey
 */
public class PipelineNodeGraphBuilder2 implements NodeGraphBuilder{
    //TODO: This goes away once union is implemented using ForkScanner
    private final Map<FlowNode, List<FlowNode>> parentToChildrenMap = new LinkedHashMap<>();

    public final WorkflowRun run;

    public PipelineNodeGraphBuilder2(WorkflowRun run) {
        this.run = run;
    }

//    public List<BluePipelineStep> getAllSteps(Link parent){
//        List<BluePipelineStep> steps = new ArrayList<>();
//        for(FlowNodeWrapper n: visitor.nodes){
//            for(FlowNodeWrapper s:n.steps){
//                steps.add(new PipelineStepImpl(s,parent));
//            }
//        }
//        return steps;
//    }
//
//    public List<FlowNodeWrapper> getAllSteps(){
//        List<FlowNodeWrapper> steps = new ArrayList<>();
//        for(FlowNodeWrapper n: visitor.nodes){
//            for(FlowNodeWrapper s:n.steps){
//                steps.add(s);
//            }
//        }
//        return steps;
//    }


    @Override
    public List<FlowNodeWrapper> getPipelineNodes() {
        return null;
    }

    @Override
    public List<BluePipelineNode> getPipelineNodes(Link parentLink) {
        PipelineNodeGraphVisitor visitor = new PipelineNodeGraphVisitor(run);
        ForkScanner.visitSimpleChunks(run.getExecution().getCurrentHeads(), visitor, new StageChunkFinder());

        List<BluePipelineNode> nodes = new ArrayList<>();
        for(FlowNodeWrapper n: visitor.nodes){
            nodes.add(new PipelineNodeImpl(n,parentLink, run));
        }
        return nodes;
    }

    @Override
    public List<BluePipelineStep> getPipelineNodeSteps(final String nodeId, Link parent) {
        DepthFirstScanner depthFirstScanner = new DepthFirstScanner();
        //If blocked scope, get the end node
        FlowNode n = depthFirstScanner.findFirstMatch(run.getExecution().getCurrentHeads(), new Predicate<FlowNode>() {
            @Override
            public boolean apply(@Nullable FlowNode input) {
                return (input!= null && input.getId().equals(nodeId) &&
                    (PipelineNodeUtil.isStage(input) || PipelineNodeUtil.isParallelBranch(input)));
            }
        });

        if(n == null){ //if no node found or the node is not stage or parallel we return empty steps
            return Collections.emptyList();
        }
        PipelineStepVisitor visitor = new PipelineStepVisitor(run, n);
        ForkScanner.visitSimpleChunks(run.getExecution().getCurrentHeads(), visitor, new StageChunkFinder());
        List<BluePipelineStep> steps = new ArrayList<>();
        for(FlowNodeWrapper node: visitor.getSteps()){
            steps.add(new PipelineStepImpl(node, parent));
        }
        return steps;
    }

    @Override
    public List<BluePipelineStep> getPipelineNodeSteps(Link parent) {
        PipelineStepVisitor visitor = new PipelineStepVisitor(run, null);
        ForkScanner.visitSimpleChunks(run.getExecution().getCurrentHeads(), visitor, new StageChunkFinder());
        List<BluePipelineStep> steps = new ArrayList<>();
        for(FlowNodeWrapper node: visitor.getSteps()){
            steps.add(new PipelineStepImpl(node, parent));
        }
        return steps;
    }

    @Override
    public BluePipelineStep getPipelineNodeStep(String id, Link parent) {
        PipelineStepVisitor visitor = new PipelineStepVisitor(run, null);
        ForkScanner.visitSimpleChunks(run.getExecution().getCurrentHeads(), visitor, new StageChunkFinder());
        FlowNodeWrapper node = visitor.getStep(id);
        return new PipelineStepImpl(node, parent);
    }


    @Override
    public List<BluePipelineNode> union(List<FlowNodeWrapper> that, Link parent) {
        return null;
    }

    private boolean isEnd(FlowNode n){
        return n instanceof StepEndNode;
    }


    private FlowNode getParentStageOfBranch(FlowNode node) {
        if (node.getParents().size() == 0) {
            return null;
        }
        FlowNode p = node.getParents().get(0);
        if (PipelineNodeUtil.isStage(p)) {
            return p;
        }
        return getParentStageOfBranch(p);
    }

    private List<FlowNode> addChild(FlowNode parent, FlowNode child) {
        List<FlowNode> children = parentToChildrenMap.get(parent);
        if (children == null) {
            children = new ArrayList<>();
            parentToChildrenMap.put(parent, children);
        }
        if (child != null) {
            children.add(child);
        }
        return children;
    }

//    public void dumpNodes(List<FlowNode> nodes) {
//        for(FlowNodeWrapper n: visitor.nodes){
//            System.out.println(String.format("id: %s, name: %s, startTime: %s, type: %s", n.getId(),
//                n.getNode().getDisplayName(), n.getTiming().getStartTimeMillis(), n.getClass()));
//            System.out.print("\tChildren: ");
//            for(String e: n.edges){
//                FlowNodeWrapper c = visitor.nodeMap.get(e);
//                System.out.print(String.format("\n\tid: %s, name: %s, startTime: %s, type: %s", c.getId(),
//                    c.getNode().getDisplayName(), c.getTiming().getStartTimeMillis(), c.getClass()));
//            }
//            System.out.println("");
//        }
//    }

    public static class NodeRunStatus {
        private final BlueRun.BlueRunResult result;
        private final BlueRun.BlueRunState state;

        public NodeRunStatus(FlowNode endNode) {
            if (endNode.getError() != null) {
                this.result = BlueRun.BlueRunResult.FAILURE;
                this.state = endNode.isRunning() ? BlueRun.BlueRunState.RUNNING : BlueRun.BlueRunState.FINISHED;
            }else if (endNode.isRunning()) {
                this.result = BlueRun.BlueRunResult.UNKNOWN;
                this.state = BlueRun.BlueRunState.RUNNING;
            } else if (NotExecutedNodeAction.isExecuted(endNode)) {
                this.result = PipelineNodeUtil.getStatus(endNode.getError());
                this.state = BlueRun.BlueRunState.FINISHED;
            } else {
                this.result = BlueRun.BlueRunResult.NOT_BUILT;
                this.state = BlueRun.BlueRunState.QUEUED;
            }
        }

        public NodeRunStatus(BlueRun.BlueRunResult result, BlueRun.BlueRunState state) {
            this.result = result;
            this.state = state;
        }


        public NodeRunStatus(GenericStatus status){
            if (status == null) {
                this.result = BlueRun.BlueRunResult.NOT_BUILT;
                this.state = BlueRun.BlueRunState.QUEUED;
                return;
            }
            switch (status) {
                case PAUSED_PENDING_INPUT:
                    this.result =  BlueRun.BlueRunResult.UNKNOWN;
                    this.state =  BlueRun.BlueRunState.PAUSED;
                    break;
                case ABORTED:
                    this.result =  BlueRun.BlueRunResult.ABORTED;
                    this.state =  BlueRun.BlueRunState.FINISHED;
                    break;
                case FAILURE:
                    this.result =  BlueRun.BlueRunResult.FAILURE;
                    this.state =  BlueRun.BlueRunState.FINISHED;
                    break;
                case IN_PROGRESS:
                    this.result =  BlueRun.BlueRunResult.UNKNOWN;
                    this.state =  BlueRun.BlueRunState.RUNNING;
                    break;
                case UNSTABLE:
                    this.result =  BlueRun.BlueRunResult.UNSTABLE;
                    this.state =  BlueRun.BlueRunState.FINISHED;
                    break;
                case SUCCESS:
                    this.result =  BlueRun.BlueRunResult.SUCCESS;
                    this.state =  BlueRun.BlueRunState.FINISHED;
                    break;
                case NOT_EXECUTED:
                    this.result = BlueRun.BlueRunResult.NOT_BUILT;
                    this.state = BlueRun.BlueRunState.QUEUED;
                    break;
                default:
                    // Shouldn't happen, above includes all statuses
                    this.result = BlueRun.BlueRunResult.NOT_BUILT;
                    this.state = BlueRun.BlueRunState.QUEUED;
            }
        }

        public BlueRun.BlueRunResult getResult() {
            return result;
        }

        public BlueRun.BlueRunState getState() {
            return state;
        }
    }

    public static class InactiveFlowNodeWrapper extends FlowNodeWrapper {

        private final FlowNodeWrapper inactiveNode;

        public InactiveFlowNodeWrapper(FlowNodeWrapper node) {
            super(node.getNode(), new PipelineNodeGraphBuilder.NodeRunStatus(null,null), new TimingInfo());
            addEdges(node.edges);
            addParents(node.getParents());
            this.inactiveNode = node;
        }

    }
}
