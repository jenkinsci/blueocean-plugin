package io.jenkins.blueocean.rest.impl.pipeline;

import com.google.common.collect.Iterables;
import io.jenkins.blueocean.rest.hal.Link;
import io.jenkins.blueocean.rest.model.BluePipelineNode;
import io.jenkins.blueocean.rest.model.BluePipelineStep;
import io.jenkins.blueocean.rest.model.BlueRun;
import org.jenkinsci.plugins.workflow.actions.LabelAction;
import org.jenkinsci.plugins.workflow.actions.NotExecutedNodeAction;
import org.jenkinsci.plugins.workflow.actions.StageAction;
import org.jenkinsci.plugins.workflow.actions.TimingAction;
import org.jenkinsci.plugins.workflow.cps.nodes.StepAtomNode;
import org.jenkinsci.plugins.workflow.graph.FlowGraphWalker;
import org.jenkinsci.plugins.workflow.graph.FlowNode;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;
import org.jenkinsci.plugins.workflow.pipelinegraphanalysis.TimingInfo;
import org.jenkinsci.plugins.workflow.support.visualization.table.FlowGraphTable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import static io.jenkins.blueocean.rest.impl.pipeline.PipelineNodeUtil.isParallelBranch;
import static io.jenkins.blueocean.rest.impl.pipeline.PipelineNodeUtil.isStage;

/**
 * Filters {@link FlowGraphTable} to BlueOcean specific model representing DAG like graph objects
 *
 * @author Vivek Pandey
 * @deprecated Use {@link PipelineNodeGraphVisitor}
 */
public class PipelineNodeGraphBuilder implements NodeGraphBuilder{

    private final List<FlowNode> sortedNodes;

    private final WorkflowRun run;
    private final Map<FlowNode, List<FlowNode>> parentToChildrenMap = new LinkedHashMap<>();
    private final Map<FlowNode, NodeRunStatus> nodeStatusMap = new LinkedHashMap<>();


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

        if(run.getExecution() != null) {
            Iterables.addAll(nodeTreeSet, new FlowGraphWalker(run.getExecution()));
        }
        this.sortedNodes = Collections.unmodifiableList(new ArrayList<>(nodeTreeSet));
//        dumpNodes(sortedNodes);
        build();

    }

    private void build(){
        FlowNode previousStage = null;
        FlowNode previousBranch = null;
        int count = 0;
        for (FlowNode node : sortedNodes) {
            if(!isStage(node) && !isParallelBranch(node)){
                continue;
            }
            boolean nestedInParallel = PipelineNodeUtil.isNestedInParallel(sortedNodes, node);
            if (isStage(node) && !nestedInParallel) { //Stage but not nested
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
                    addChild(previousStage, node);
                } else { //previousStage is null
                    addChild(node, null);
                }

                if(node.getAction(LabelAction.class) != null && node.getAction(StageAction.class) == null){
                    FlowNode endNode = PipelineNodeUtil.getStepEndNode(sortedNodes, node);
                    if(endNode == null){
                        endNode = PipelineNodeUtil.getEndNode(sortedNodes,node);
                    }
                    if(endNode != null){
                        nodeStatusMap.put(node, new NodeRunStatus(endNode));
                    }
                }else if(previousStage!=null){
                    nodeStatusMap.put(previousStage, new NodeRunStatus(sortedNodes.get(count - 1)));
                }
                previousStage = node;
            } else if (isParallelBranch(node) && !nestedInParallel) { //branch but not nested ones
                addChild(node, null);
                if(previousStage != null) {
                    addChild(previousStage, node);
                }
                FlowNode endNode = PipelineNodeUtil.getStepEndNode(sortedNodes,node);
                if (endNode != null) {
                    nodeStatusMap.put(node, new NodeRunStatus(endNode));
                }else{
                    //It's still running, report it as state: running and result: unknown
                    nodeStatusMap.put(node, new NodeRunStatus(BlueRun.BlueRunResult.UNKNOWN, BlueRun.BlueRunState.RUNNING));
                }
                previousBranch = node;
            }
            count++;
        }
        int size = parentToChildrenMap.keySet().size();
        if (size > 0) {
            NodeRunStatus runStatus = PipelineNodeUtil.getStatus(run);
            FlowNode lastNode = getLastStageNode();
            nodeStatusMap.put(lastNode, runStatus);
        }
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
        if(isStage(node)){
            return getStageSteps(node);
        }else if(isParallelBranch(node)){
            return getParallelBranchSteps(node);
        }
        return Collections.emptyList();
    }

    public List<FlowNode> getStageSteps(FlowNode p){
        List<FlowNode> steps = new ArrayList<>();
        int i = sortedNodes.indexOf(p);
        if(i>=0 && isStage(p)){
            FlowNode end = PipelineNodeUtil.getStepEndNode(sortedNodes,p);
            //collect steps till next stage is found otherwise till end of pipeline
            for(int j=i+1; j < sortedNodes.size(); j++){
                FlowNode c = sortedNodes.get(j);
                //if the stage is not nested ignore them, because we want to include steps from nested stages
                if(isStage(c) && !PipelineNodeUtil.isInBlock(p,end,c)){
                    break;
                }
                if(c instanceof StepAtomNode) {
                    steps.add(c);
                }
            }
        }
        return steps;
    }

    public List<FlowNode> getAllSteps(){
        List<FlowNode> steps = new ArrayList<>();
        for(FlowNode c: sortedNodes){
            if(c instanceof StepAtomNode && !isStage(c)) {
                steps.add(c);
            }
        }
        return steps;
    }

    public List<FlowNode> getParallelBranchSteps(FlowNode p){
        List<FlowNode> steps = new ArrayList<>();
        int i = sortedNodes.indexOf(p);
        FlowNode prev=p;
        if(i>=0 && isParallelBranch(p)){
            FlowNode end = PipelineNodeUtil.getStepEndNode(sortedNodes,p);
            for(int j=i+1; j < sortedNodes.size(); j++){
                FlowNode c = sortedNodes.get(j);
                if(c.equals(end)){
                    nodeStatusMap.put(p, new NodeRunStatus(end));
                    break;
                }
                if(isParallelBranch(c)){
                    continue;
                }
                //we take only the legal children
                if(!PipelineNodeUtil.isInBlock(p, end, c) && !isParentOf(c,prev)){
                    continue;
                }
                if(c instanceof StepAtomNode) {
                    steps.add(c);
                    prev=c;

                    FlowNode endNode = PipelineNodeUtil.getStepEndNode(sortedNodes,c);
                    if (endNode != null) {
                        nodeStatusMap.put(c, new NodeRunStatus(endNode));
                    }
                }
            }
        }
        return steps;
    }

    private boolean isParentOf(FlowNode node, FlowNode child){
        for(FlowNode n:node.getParents()){
            if(child.equals(n)){
                return true;
            }
        }
        return false;
    }
    
    @Override
    public List<FlowNodeWrapper> getPipelineNodes() {
        List<FlowNodeWrapper> nodes = new ArrayList<>();
        for (FlowNode n : parentToChildrenMap.keySet()) {
            NodeRunStatus status = nodeStatusMap.get(n);

            if (!isExecuted(n)) {
                status = new NodeRunStatus(BlueRun.BlueRunResult.UNKNOWN, BlueRun.BlueRunState.QUEUED);
            } else if (status == null) {
                status = getEffectiveBranchStatus(n);
            }
            long durationInMillis = 0;
            long startTime = TimingAction.getStartTime(n);
            if(status.state == BlueRun.BlueRunState.FINISHED){
                durationInMillis = getDurationInMillis(n);
            }else if(status.state == BlueRun.BlueRunState.RUNNING){
                durationInMillis = System.currentTimeMillis()-TimingAction.getStartTime(n);
            }
            FlowNodeWrapper wrapper = new FlowNodeWrapper(n, status, new TimingInfo(durationInMillis,0,startTime));
            for(FlowNode c: parentToChildrenMap.get(n)){
                wrapper.addEdge(c.getId());
            }
            nodes.add(wrapper);
        }
        return nodes;
    }

    public List<BluePipelineNode> getPipelineNodes(Link parentLink) {
        List<BluePipelineNode> nodes = new ArrayList<>();
        for (FlowNodeWrapper n : getPipelineNodes()) {
            nodes.add(new PipelineNodeImpl(n, parentLink, run));
        }
        return nodes;
    }

    @Override
    public List<BluePipelineStep> getPipelineNodeSteps(String nodeId, Link parent) {
        List<BluePipelineStep> steps = new ArrayList<>();
        List<FlowNode> flowNodes = new ArrayList<>();
        if(nodeId == null){
            flowNodes.addAll(getAllSteps());
        }else{
            flowNodes.addAll(getSteps(getNodeById(nodeId)));
        }
        for(FlowNode node: flowNodes){
            steps.add(new PipelineStepImpl(new FlowNodeWrapper(node,
                new NodeRunStatus(node),
                new TimingInfo(getDurationInMillis(node), 0, 0)), parent));
        }
        return steps;
    }

    @Override
    public List<BluePipelineStep> getPipelineNodeSteps(Link parent) {
        return getPipelineNodeSteps(null, parent);
    }

    @Override
    public BluePipelineStep getPipelineNodeStep(String id, Link parent) {
        for(BluePipelineStep step: getPipelineNodeSteps(parent)){
            if(step.getId().equals(id)){
                return step;
            }
        }
        return null;
    }


    @Override
    public List<BluePipelineNode> union(List<FlowNodeWrapper> futureNodes, Link parentLink) {

        List<FlowNodeWrapper> currentNodes = getPipelineNodes();

        int currentNodeSize = currentNodes.size();
        if (currentNodeSize < futureNodes.size()) {
            for (int i = currentNodeSize; i < futureNodes.size(); i++) {

                FlowNodeWrapper futureNode = futureNodes.get(i);

                // Add the last successful pipeline's first node to the edge of current node's last node
                if (currentNodeSize> 0 && i == currentNodeSize) {
                    FlowNodeWrapper latestNode = currentNodes.get(currentNodeSize - 1);
                    if (isStage(latestNode.getNode())) {
                        addChild(latestNode.getNode(), futureNode.getNode());
                    } else if (isParallelBranch(latestNode.getNode())) {
                        /**
                         * If its a parallel node, find all its siblings and add the next node as
                         * edge (if not already present)
                         */
                        //parallel node has at most one paraent
                        FlowNode parent = getParentStageOfBranch(latestNode.getNode());
                        if (parent != null) {
                            List<FlowNode> children = parentToChildrenMap.get(parent);
                            for (FlowNode c : children) {
                                // Add next node to the parallel node's edge
                                if (isParallelBranch(c)) {
                                    for(FlowNodeWrapper f: currentNodes){
                                        if(f.getNode().equals(c)){
                                            f.addEdge(futureNode.getId());
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                FlowNodeWrapper n = new FlowNodeWrapper(futureNode.getNode(),
                    new NodeRunStatus(null,null),
                    new TimingInfo());
                n.addEdges(futureNode.edges);
                n.addParents(futureNode.getParents());
                currentNodes.add(n);
            }
        }
        List<BluePipelineNode> newNodes = new ArrayList<>();
        for(FlowNodeWrapper n: currentNodes){
            newNodes.add(new PipelineNodeImpl(n,parentLink,run));
        }
        return newNodes;
    }

    public List<FlowNode> getChildren(FlowNode parent){
        return parentToChildrenMap.get(parent);
    }

    public long getDurationInMillis(FlowNode node){
        long startTime = TimingAction.getStartTime(node);
        if( startTime == 0){
            return 0;
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
        if(isStage(node)){
            boolean lookForNextStage = false;
            for(FlowNode n: parentToChildrenMap.keySet()){
                if(n.equals(node)){
                    lookForNextStage = true;
                    continue;
                }
                if(lookForNextStage && isStage(n)){ //we got the next stage
                    return TimingAction.getStartTime(n) - startTime;
                }
            }
        }else if(isParallelBranch(node)){
            FlowNode endNode = PipelineNodeUtil.getStepEndNode(sortedNodes,node);
            if(endNode != null){
                return TimingAction.getStartTime(endNode) - startTime;
            }
        }else if(node instanceof StepAtomNode){
            int i = sortedNodes.indexOf(node);
            if(i >=0 && i+1 < sortedNodes.size()){
                return TimingAction.getStartTime(sortedNodes.get(i+1)) - startTime;
            }
        }
        return run.getExecution()!= null && run.getExecution().isComplete()
            ? (run.getDuration() + run.getStartTimeInMillis()) - startTime
            : System.currentTimeMillis() - startTime;
    }

    private FlowNode getParentStageOfBranch(FlowNode node) {
        if (node.getParents().size() == 0) {
            return null;
        }
        FlowNode p = node.getParents().get(0);
        if (isStage(p)) {
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
            if (isStage(n)) {
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
            if (isParallelBranch(c)) {
                NodeRunStatus s = nodeStatusMap.get(c);
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

        return new NodeRunStatus(result, state);
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
            System.out.println(String.format("id: %s, name: %s, startTime: %s, type: %s, parent: %s", n.getId(), n.getDisplayName(), TimingAction.getStartTime(n), n.getClass(), n.getParents().size() > 0 ? n.getParents().get(0).getId(): null));
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

    public List<FlowNode> getSages(){
        List<FlowNode> stages = new ArrayList<>();

        for(FlowNode n: parentToChildrenMap.keySet()){
            if(isStage(n)){
                stages.add(n);
            }
        }
        return stages;
    }

    public List<FlowNode> getParallelBranches(){
        List<FlowNode> parallel = new ArrayList<>();

        for(FlowNode n: parentToChildrenMap.keySet()){
            if(isParallelBranch(n)){
                parallel.add(n);
            }
        }
        return parallel;
    }
}
