package io.jenkins.blueocean.rest.impl.pipeline;

import com.google.common.collect.ImmutableList;
import io.jenkins.blueocean.rest.hal.Link;
import io.jenkins.blueocean.rest.model.BluePipelineNode;
import io.jenkins.blueocean.rest.model.BluePipelineStep;
import io.jenkins.blueocean.rest.model.BlueRun;
import org.jenkinsci.plugins.workflow.actions.NotExecutedNodeAction;
import org.jenkinsci.plugins.workflow.cps.nodes.StepEndNode;
import org.jenkinsci.plugins.workflow.graph.FlowNode;
import org.jenkinsci.plugins.workflow.graphanalysis.ForkScanner;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;
import org.jenkinsci.plugins.workflow.pipelinegraphanalysis.GenericStatus;
import org.jenkinsci.plugins.workflow.pipelinegraphanalysis.StageChunkFinder;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Vivek Pandey
 */
public class PipelineNodeGraphBuilder2{
    //TODO: This goes away once union is implemented using ForkScanner
    private final Map<FlowNode, List<FlowNode>> parentToChildrenMap = new LinkedHashMap<>();

    public final PipelineNodeGraphVisitor visitor;
    public PipelineNodeGraphBuilder2(WorkflowRun run) {
        this.visitor = new PipelineNodeGraphVisitor(run);
        ForkScanner.visitSimpleChunks(run.getExecution().getCurrentHeads(), visitor, new StageChunkFinder());
    }

    public List<BluePipelineStep> getAllSteps(Link parent){
        List<BluePipelineStep> steps = new ArrayList<>();
        for(FlowNodeWrapper n: visitor.nodes){
            for(FlowNodeWrapper s:n.steps){
                steps.add(new PipelineStepImpl(s,parent));
            }
        }
        return steps;
    }

    public List<FlowNodeWrapper> getAllSteps(){
        List<FlowNodeWrapper> steps = new ArrayList<>();
        for(FlowNodeWrapper n: visitor.nodes){
            for(FlowNodeWrapper s:n.steps){
                steps.add(s);
            }
        }
        return steps;
    }



    /**
     * Create a union of current pipeline nodes with the one from future. Term future indicates that
     * this list of nodes are either in the middle of processing or failed somewhere in middle and we are
     * projecting future nodes in the pipeline.
     * <p>
     * Last element of this node is patched to point to the first node of given list. First node of given
     * list is indexed at thisNodeList.size().
     *
     * @param other Other {@link PipelineNodeGraphBuilder} to create union with
     * @return list of FlowNode that is union of current set of nodes and the given list of nodes. If futureNodes
     * are not bigger than this pipeline nodes then no union is performed.
     * @see PipelineNodeContainerImpl#PipelineNodeContainerImpl(WorkflowRun, Link)
     */
    public List<BluePipelineNode> union(PipelineNodeGraphBuilder2 other, Link parentLink) {
        Map<FlowNode, List<FlowNode>> futureNodes = other.parentToChildrenMap;
        if (parentToChildrenMap.size() < futureNodes.size()) {

            // XXX: If the pipeline was modified since last successful run then
            // the union might represent invalid future nodes.
            List<FlowNode> nodes = ImmutableList.copyOf(parentToChildrenMap.keySet());
            List<FlowNode> thatNodes = ImmutableList.copyOf(futureNodes.keySet());
            int currentNodeSize = nodes.size();
            for (int i = nodes.size(); i < futureNodes.size(); i++) {
                PipelineNodeGraphBuilder2.InactiveFlowNodeWrapper n = new PipelineNodeGraphBuilder2.InactiveFlowNodeWrapper(thatNodes.get(i));

                // Add the last successful pipeline's first node to the edge of current node's last node
                if (currentNodeSize> 0 && i == currentNodeSize) {
                    FlowNode latestNode = nodes.get(currentNodeSize - 1);
                    if (PipelineNodeUtil.isStage(latestNode)) {
                        addChild(latestNode, n);
                    } else if (PipelineNodeUtil.isParallelBranch(latestNode)) {
                        /**
                         * If its a parallel node, find all its siblings and add the next node as
                         * edge (if not already present)
                         */
                        //parallel node has at most one paraent
                        FlowNode parent = getParentStageOfBranch(latestNode);
                        if (parent != null) {
                            List<FlowNode> children = parentToChildrenMap.get(parent);
                            for (FlowNode c : children) {
                                // Add next node to the parallel node's edge
                                if (PipelineNodeUtil.isParallelBranch(c)) {
                                    addChild(c, n);
                                }
                            }
                        }
                    }
                }
                parentToChildrenMap.put(n, futureNodes.get(n.inactiveNode));
            }
        }
        return getPipelineNodes(parentLink);
    }

    public List<BluePipelineNode> getPipelineNodes(Link parentLink) {
        List<BluePipelineNode> nodes = new ArrayList<>();
        for(FlowNodeWrapper n: visitor.nodes){
            nodes.add(new PipelineNodeImpl(n,parentLink));
        }
        return nodes;
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

    public void dumpNodes(List<FlowNode> nodes) {
        for(FlowNodeWrapper n: visitor.nodes){
            System.out.println(String.format("id: %s, name: %s, startTime: %s, type: %s", n.getId(),
                n.getNode().getDisplayName(), n.getTiming().getStartTimeMillis(), n.getClass()));
            System.out.print("\tChildren: ");
            for(String e: n.edges){
                FlowNodeWrapper c = visitor.nodeMap.get(e);
                System.out.print(String.format("\n\tid: %s, name: %s, startTime: %s, type: %s", c.getId(),
                    c.getNode().getDisplayName(), c.getTiming().getStartTimeMillis(), c.getClass()));
            }
            System.out.println("");
        }
    }

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

    public static class InactiveFlowNodeWrapper extends FlowNode {

        private final FlowNode inactiveNode;

        public InactiveFlowNodeWrapper(FlowNode node) {
            super(node.getExecution(), node.getId());
            this.inactiveNode = node;
        }

        @Override
        protected String getTypeDisplayName() {
            return PipelineNodeUtil.getDisplayName(inactiveNode);
        }
    }
}
