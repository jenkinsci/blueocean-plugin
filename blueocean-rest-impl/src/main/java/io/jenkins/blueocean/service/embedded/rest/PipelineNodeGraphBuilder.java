package io.jenkins.blueocean.service.embedded.rest;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import io.jenkins.blueocean.rest.model.BluePipelineNode;
import io.jenkins.blueocean.rest.model.BlueRun;
import org.jenkinsci.plugins.workflow.actions.NotExecutedNodeAction;
import org.jenkinsci.plugins.workflow.actions.TimingAction;
import org.jenkinsci.plugins.workflow.cps.nodes.StepAtomNode;
import org.jenkinsci.plugins.workflow.cps.nodes.StepEndNode;
import org.jenkinsci.plugins.workflow.graph.FlowGraphWalker;
import org.jenkinsci.plugins.workflow.graph.FlowNode;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;
import org.jenkinsci.plugins.workflow.support.visualization.table.FlowGraphTable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

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

        TreeSet<FlowNode> nodeTreeSet = new TreeSet<>(new Comparator<FlowNode>() {
            @Override
            public int compare(FlowNode node1, FlowNode node2) {
                return Integer.compare(parseIota(node1), parseIota(node2));
            }

            private int parseIota(FlowNode node) {
                try {
                    return Integer.parseInt(node.getId());
                } catch (NumberFormatException e) {
                    return 0;
                }
            }
        });

        Iterables.addAll(nodeTreeSet, new FlowGraphWalker(run.getExecution()));

        this.sortedNodes = Collections.unmodifiableList(new ArrayList<>(nodeTreeSet));
//        dumpNodes();
        build();

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
                if(previousStage != null) {
                    addChild(previousStage, node);
                }
                FlowNode endNode = getStepEndNode(node);
                if (endNode != null) {
                    nodeStatusMap.put(node, new PipelineNodeGraphBuilder.NodeRunStatus(endNode));
                }
                previousBranch = node;
            }
            count++;
        }
        int size = parentToChildrenMap.keySet().size();
        if (size > 0) {
            PipelineNodeGraphBuilder.NodeRunStatus runStatus = PipelineNodeUtil.getStatus(run);
            FlowNode lastNode = getLastStageNode();
            nodeStatusMap.put(lastNode, runStatus);
        }
    }

    private FlowNode getStepEndNode(FlowNode startNode){
        for(int i = sortedNodes.size() - 1; i >=0; i--){
            FlowNode n = sortedNodes.get(i);
            if(isEnd(n)){
                StepEndNode endNode = (StepEndNode) n;
                if(endNode.getStartNode().equals(startNode))
                    return endNode;
            }
        }
        return null;
    }

    public FlowNode getNodeById(String id){
        for(FlowNode node: sortedNodes){
            if(node.getId().equals(id)){
                return node;
            }
        }
        return null;
    }

    public List<FlowNode> getSteps(FlowNode node){
        if(PipelineNodeUtil.isStage(node)){
            return getStageSteps(node);
        }else if(PipelineNodeUtil.isParallelBranch(node)){
            return getParallelBranchSteps(node);
        }
        return Collections.emptyList();
    }

    public List<FlowNode> getStageSteps(FlowNode p){
        List<FlowNode> steps = new ArrayList<>();
        int i = sortedNodes.indexOf(p);
        if(i>=0 && PipelineNodeUtil.isStage(p)){
            //collect steps till next stage is found otherwise till end of pipeline
            for(int j=i+1; j < sortedNodes.size(); j++){
                FlowNode c = sortedNodes.get(j);
                if(PipelineNodeUtil.isStage(c)){
                    break;
                }
                if(c instanceof StepAtomNode) {
                    steps.add(c);
                }
            }
        }
        return steps;
    }

    public List<FlowNode> getParallelBranchSteps(FlowNode p){
        List<FlowNode> steps = new ArrayList<>();
        int i = sortedNodes.indexOf(p);
        if(i>=0 && PipelineNodeUtil.isParallelBranch(p)){
            FlowNode end = getStepEndNode(p);
            FlowNode parent = p;
            for(int j=i+1; j < sortedNodes.size(); j++){
                FlowNode c = sortedNodes.get(j);
                if(c.equals(end)){
                    nodeStatusMap.put(p, new PipelineNodeGraphBuilder.NodeRunStatus(end));
                    break;
                }
                if(PipelineNodeUtil.isParallelBranch(c)){
                    continue;
                }
                //we take only the legal children
                if(!c.getParents().contains(parent)){
                    continue;
                }else{
                    parent = c;
                }

                if(c instanceof StepAtomNode) {
                    steps.add(c);

                    FlowNode endNode = getStepEndNode(c);
                    if (endNode != null) {
                        nodeStatusMap.put(c, new PipelineNodeGraphBuilder.NodeRunStatus(endNode));
                    }
                }
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
            nodes.add(new PipelineNodeImpl(run, n, status, this));
        }
        return nodes;
    }

    public List<FlowNode> getChildren(FlowNode parent){
        return parentToChildrenMap.get(parent);
    }

    public Long getDurationInMillis(FlowNode node){
        long startTime = TimingAction.getStartTime(node);
        if( startTime == 0){
            return null;
        }
        /**
         * For Stage node:
         *
         * Find next stage node
         * duration = nextStageNodeStartTime - thisStageNode.startTime
         *
         * For Parallel node:
         *
         * Find the endNode of the parallel branch
         * duration = endNode.startTime - thisNode.startTime
         *
         * If this happens to be the last stage or parallel node in the pipeline
         * duration = pipelineEndTime - thisStageNode.startTime
         *
         */
        if(PipelineNodeUtil.isStage(node)){
            boolean lookForNextStage = false;
            for(FlowNode n: parentToChildrenMap.keySet()){
                if(n.equals(node)){
                    lookForNextStage = true;
                    continue;
                }
                if(lookForNextStage && PipelineNodeUtil.isStage(n)){ //we got the next stage
                    return TimingAction.getStartTime(n) - startTime;
                }
            }
        }else if(PipelineNodeUtil.isParallelBranch(node)){
            FlowNode endNode = getStepEndNode(node);
            if(endNode != null){
                return TimingAction.getStartTime(endNode) - startTime;
            }
        }else if(node instanceof StepAtomNode){
            int i = sortedNodes.indexOf(node);
            if(i >=0 && i+1 < sortedNodes.size()){
                return TimingAction.getStartTime(sortedNodes.get(i+1)) - startTime;
            }
        }
        return run.getExecution().isComplete()
            ? (run.getDuration() + run.getStartTimeInMillis()) - startTime
            : System.currentTimeMillis() - startTime;
    }

    private boolean isEnd(FlowNode n){
        return n instanceof StepEndNode;
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

    public void dumpNodes(List<FlowNode> nodes) {
        for (FlowNode n : nodes) {
            System.out.println(String.format("id: %s, name: %s, startTime: %s, type: %s", n.getId(), n.getDisplayName(), TimingAction.getStartTime(n), n.getClass()));
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
