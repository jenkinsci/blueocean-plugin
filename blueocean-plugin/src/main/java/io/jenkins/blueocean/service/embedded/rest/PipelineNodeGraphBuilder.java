package io.jenkins.blueocean.service.embedded.rest;

import com.google.common.collect.ImmutableList;
import io.jenkins.blueocean.rest.model.BluePipelineNode;
import io.jenkins.blueocean.rest.model.BlueRun;
import org.jenkinsci.plugins.workflow.actions.NotExecutedNodeAction;
import org.jenkinsci.plugins.workflow.actions.TimingAction;
import org.jenkinsci.plugins.workflow.graph.FlowNode;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;
import org.jenkinsci.plugins.workflow.support.visualization.table.FlowGraphTable;

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
public class PipelineNodeGraphBuilder {

    private final List<FlowNode> sortedNodes;

    private final WorkflowRun run;
    private final Map<FlowNode, List<FlowNode>> parentToChildrenMap = new LinkedHashMap<>();
    private final Map<FlowNode, PipelineNodeGraphBuilder.NodeRunStatus> nodeStatusMap = new LinkedHashMap<>();


    public PipelineNodeGraphBuilder(WorkflowRun run) {
        this.run = run;

        FlowGraphTable nodeGraphTable = new FlowGraphTable(run.getExecution());
        nodeGraphTable.build();
        List<FlowNode> nodes = new ArrayList<>();
        for (FlowGraphTable.Row r : nodeGraphTable.getRows()) {
            nodes.add(r.getNode());
        }
        this.sortedNodes = Collections.unmodifiableList(nodes);
        build();
        //dumpNodes();
    }

    private void build(){
        FlowNode previousStage = null;
        FlowNode previousBranch = null;
        int count = 0;
        for (FlowNode node : sortedNodes) {
            if (PipelineNodeUtil.isStage(node)) { //Stage
                addChild(node, null);
                if (previousBranch != null) {
                    /**
                     * We encountered stage after previous branch, we need to
                     *  - get all branches from previous stage
                     *  - add this stage node as child to all branches
                     */
                    List<FlowNode> branches = parentToChildrenMap.get(previousStage);
                    for (FlowNode n : branches) {
                        addChild(n, node);
                    }
                    nodeStatusMap.put(previousBranch,
                        new PipelineNodeGraphBuilder.NodeRunStatus(sortedNodes.get(count - 1)));
                    previousBranch = null;
                } else if (previousStage != null) {
                    //node before this stage is the last node before previousStage stage
                    //get error condition on this node
                    //XXX: This is assuming stage don't nest stage
                    nodeStatusMap.put(previousStage, new PipelineNodeGraphBuilder.NodeRunStatus(sortedNodes.get(count - 1)));
                    addChild(previousStage, node);
                } else { //previousStage is null
                    addChild(node, null);
                }
                previousStage = node;
            } else if (PipelineNodeUtil.isParallelBranch(node)) { //branch
                addChild(node, null);
                addChild(previousStage, node);
                if (previousBranch != null) {
                    nodeStatusMap.put(previousBranch, new PipelineNodeGraphBuilder.NodeRunStatus(sortedNodes.get(count - 1)));
                }
                previousBranch = node;
            }
            count++;
        }
        int size = parentToChildrenMap.keySet().size();
        if (size > 0) {
            PipelineNodeGraphBuilder.NodeRunStatus runStatus = new PipelineNodeGraphBuilder.NodeRunStatus(sortedNodes.get(sortedNodes.size() - 1));
            FlowNode lastNode = getLastNode();
            nodeStatusMap.put(lastNode, runStatus);
        }
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
     * @see PipelineNodeContainerImpl#PipelineNodeContainerImpl(WorkflowRun)
     */
    public List<BluePipelineNode> union(PipelineNodeGraphBuilder other) {
        Map<FlowNode, List<FlowNode>> futureNodes = other.parentToChildrenMap;
        if (parentToChildrenMap.size() < futureNodes.size()) {

            // XXX: If the pipeline was modified since last successful run then
            // the union might represent invalid future nodes.
            List<FlowNode> nodes = ImmutableList.copyOf(parentToChildrenMap.keySet());
            List<FlowNode> thatNodes = ImmutableList.copyOf(futureNodes.keySet());
            int currentNodeSize = nodes.size();
            for (int i = nodes.size(); i < futureNodes.size(); i++) {
                InactiveFlowNodeWrapper n = new InactiveFlowNodeWrapper(thatNodes.get(i));

                // Add the last successful pipeline's first node to the edge of current node's last node
                if (i == currentNodeSize) {
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
        return getPipelineNodes();
    }

    public List<BluePipelineNode> getPipelineNodes() {
        List<BluePipelineNode> nodes = new ArrayList<>();
        for (FlowNode n : parentToChildrenMap.keySet()) {
            PipelineNodeGraphBuilder.NodeRunStatus status = nodeStatusMap.get(n);

            if (!isExecuted(n)) {
                status = new PipelineNodeGraphBuilder.NodeRunStatus(BlueRun.BlueRunResult.UNKNOWN, BlueRun.BlueRunState.QUEUED);
            } else if (status == null) {
                status = getEffectiveBranchStatus(n);
            }
            nodes.add(new PipelineNodeImpl(run, n, status, parentToChildrenMap.get(n)));
        }
        return nodes;
    }

    private FlowNode getLastNode() {
        if (parentToChildrenMap.keySet().isEmpty()) {
            return null;
        }
        FlowNode node = null;
        for (FlowNode n : parentToChildrenMap.keySet()) {
            node = n;
        }
        return node;
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

    private FlowNode getLastStageNode() {
        if (parentToChildrenMap.keySet().isEmpty()) {
            return null;
        }
        FlowNode node = null;
        for (FlowNode n : parentToChildrenMap.keySet()) {
            if (PipelineNodeUtil.isStage(n)) {
                node = n;
            }
        }
        return node;
    }

    private FlowNode getLastBranchNode() {
        if (parentToChildrenMap.keySet().isEmpty()) {
            return null;
        }
        FlowNode node = null;
        for (FlowNode n : parentToChildrenMap.keySet()) {
            if (PipelineNodeUtil.isParallelBranch(n)) {
                node = n;
            }
        }
        return node;
    }

    private NodeRunStatus getEffectiveBranchStatus(FlowNode n) {
        List<FlowNode> children = parentToChildrenMap.get(n);
        BlueRun.BlueRunResult result = BlueRun.BlueRunResult.SUCCESS;
        BlueRun.BlueRunState state = BlueRun.BlueRunState.FINISHED;
        boolean atLeastOneBranchDidNotSucceed = false;
        boolean atLeastOneBranchisUnknown = false;
        for (FlowNode c : children) {
            if (PipelineNodeUtil.isParallelBranch(c)) {
                PipelineNodeGraphBuilder.NodeRunStatus s = nodeStatusMap.get(c);
                if (s == null) {
                    continue;
                }
                if (!atLeastOneBranchDidNotSucceed && s.getResult() == BlueRun.BlueRunResult.FAILURE ||
                    s.getResult() == BlueRun.BlueRunResult.UNSTABLE) {
                    atLeastOneBranchDidNotSucceed = true;
                    result = s.getResult();
                }

                if (s.getResult() == BlueRun.BlueRunResult.UNKNOWN) {
                    atLeastOneBranchisUnknown = true;
                }
                if (s.getState() != BlueRun.BlueRunState.FINISHED) {
                    if (state != BlueRun.BlueRunState.RUNNING) {
                        state = s.getState();
                    }
                }
            }
        }

        if (!atLeastOneBranchDidNotSucceed && atLeastOneBranchisUnknown) {
            result = BlueRun.BlueRunResult.UNKNOWN;
        }

        return new PipelineNodeGraphBuilder.NodeRunStatus(result, state);
    }

    private boolean isExecuted(FlowNode node) {
        return NotExecutedNodeAction.isExecuted(node);
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

    public void dumpNodes() {
        for (FlowNode n : sortedNodes) {
            System.out.println(String.format("id: %s, name: %s, startTime: %s", n.getId(), n.getDisplayName(), TimingAction.getStartTime(n)));
        }
    }

    public static class NodeRunStatus {
        private final BlueRun.BlueRunResult result;
        private final BlueRun.BlueRunState state;

        public NodeRunStatus(FlowNode endNode) {
            if (endNode.isRunning()) {
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
