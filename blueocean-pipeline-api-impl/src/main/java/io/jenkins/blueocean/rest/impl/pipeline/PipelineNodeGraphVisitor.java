package io.jenkins.blueocean.rest.impl.pipeline;

import com.google.common.base.Predicate;
import io.jenkins.blueocean.rest.hal.Link;
import io.jenkins.blueocean.rest.model.BluePipelineNode;
import io.jenkins.blueocean.rest.model.BluePipelineStep;
import io.jenkins.blueocean.rest.model.BlueRun;
import org.jenkinsci.plugins.workflow.actions.NotExecutedNodeAction;
import org.jenkinsci.plugins.workflow.actions.TimingAction;
import org.jenkinsci.plugins.workflow.cps.nodes.StepAtomNode;
import org.jenkinsci.plugins.workflow.cps.nodes.StepEndNode;
import org.jenkinsci.plugins.workflow.cps.nodes.StepStartNode;
import org.jenkinsci.plugins.workflow.graph.BlockEndNode;
import org.jenkinsci.plugins.workflow.graph.FlowNode;
import org.jenkinsci.plugins.workflow.graphanalysis.DepthFirstScanner;
import org.jenkinsci.plugins.workflow.graphanalysis.ForkScanner;
import org.jenkinsci.plugins.workflow.graphanalysis.MemoryFlowChunk;
import org.jenkinsci.plugins.workflow.graphanalysis.StandardChunkVisitor;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;
import org.jenkinsci.plugins.workflow.pipelinegraphanalysis.GenericStatus;
import org.jenkinsci.plugins.workflow.pipelinegraphanalysis.StageChunkFinder;
import org.jenkinsci.plugins.workflow.pipelinegraphanalysis.StatusAndTiming;
import org.jenkinsci.plugins.workflow.pipelinegraphanalysis.TimingInfo;
import org.jenkinsci.plugins.workflow.support.actions.PauseAction;
import org.jenkinsci.plugins.workflow.support.steps.input.InputAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

/**
 * @author Vivek Pandey
 */
public class PipelineNodeGraphVisitor extends StandardChunkVisitor implements NodeGraphBuilder{
    private final WorkflowRun run;

    private final ArrayDeque<FlowNodeWrapper> parallelBranches = new ArrayDeque<>();

    public final ArrayDeque<FlowNodeWrapper> nodes = new ArrayDeque<>();

    private FlowNode firstExecuted = null;

    private FlowNodeWrapper nextStage;

    private FlowNode parallelEnd;

    public final Map<String, FlowNodeWrapper> nodeMap = new LinkedHashMap<>();

    private static final Logger logger = LoggerFactory.getLogger(PipelineNodeGraphVisitor.class);

    private static final boolean isNodeVisitorDumpEnabled = Boolean.getBoolean("NODE-DUMP-ENABLED");

    private final Stack<FlowNode> nestedStages = new Stack<>();
    private final Stack<FlowNode> nestedbranches = new Stack<>();

    private final ArrayDeque<FlowNode> pendingInputSteps = new ArrayDeque<>();

    private final Stack<FlowNode> parallelBranchEndNodes = new Stack<>();

    private final InputAction inputAction;

    public PipelineNodeGraphVisitor(WorkflowRun run) {
        this.run = run;
        this.inputAction = run.getAction(InputAction.class);
        if(run.getExecution()!=null) {
            ForkScanner.visitSimpleChunks(run.getExecution().getCurrentHeads(), this, new StageChunkFinder());
        }
    }

    @Override
    public void chunkStart(@Nonnull FlowNode startNode, @CheckForNull FlowNode beforeBlock, @Nonnull ForkScanner scanner) {
        super.chunkStart(startNode, beforeBlock, scanner);
        if(isNodeVisitorDumpEnabled)
            dump(String.format("chunkStart=> id: %s, name: %s, function: %s", startNode.getId(),
                    startNode.getDisplayName(), startNode.getDisplayFunctionName()));

        if(PipelineNodeUtil.isSyntheticStage(startNode)){
            return;
        }
        if (NotExecutedNodeAction.isExecuted(startNode)) {
            firstExecuted = startNode;
        }

    }

    private StepStartNode agentNode = null;

    @Override
    public void chunkEnd(@Nonnull FlowNode endNode, @CheckForNull FlowNode afterBlock, @Nonnull ForkScanner scanner) {
        super.chunkEnd(endNode, afterBlock, scanner);

        if(isNodeVisitorDumpEnabled)
            dump(String.format("chunkEnd=> id: %s, name: %s, function: %s, type:%s", endNode.getId(),
                    endNode.getDisplayName(), endNode.getDisplayFunctionName(), endNode.getClass()));

        if(isNodeVisitorDumpEnabled && endNode instanceof StepEndNode){
            dump("\tStartNode: "+((StepEndNode) endNode).getStartNode());
        }

        if(endNode instanceof StepStartNode){
            if(endNode.getDisplayFunctionName().equals("node")){
                agentNode = (StepStartNode) endNode;
            }
        }
        //if block stage node push it to stack as it may have nested stages
        if(endNode instanceof StepEndNode
                && !PipelineNodeUtil.isSyntheticStage(((StepEndNode) endNode).getStartNode()) //skip synthetic stages
                && PipelineNodeUtil.isStage(((StepEndNode) endNode).getStartNode())) {
            nestedStages.push(endNode);
        }
        firstExecuted = null;

        // if we're using marker-based (and not block-scoped) stages, add the last node as part of its contents
        if (!(endNode instanceof BlockEndNode)) {
            atomNode(null, endNode, afterBlock, scanner);
        }
    }

    @Override
    public void parallelStart(@Nonnull FlowNode parallelStartNode, @Nonnull FlowNode branchNode, @Nonnull ForkScanner scanner) {
        if(isNodeVisitorDumpEnabled) {
            dump(String.format("parallelStart=> id: %s, name: %s, function: %s", parallelStartNode.getId(),
                    parallelStartNode.getDisplayName(), parallelStartNode.getDisplayFunctionName()));
            dump(String.format("\tbranch=> id: %s, name: %s, function: %s", branchNode.getId(),
                    branchNode.getDisplayName(), branchNode.getDisplayFunctionName()));
        }

        assert nestedbranches.size() == parallelBranchEndNodes.size();

        while(!nestedbranches.empty() && !parallelBranchEndNodes.empty()){
            FlowNode branchStartNode = nestedbranches.pop();

            FlowNode endNode = parallelBranchEndNodes.pop();

            TimingInfo times;
            NodeRunStatus status;

            if(endNode != null) {
                times = StatusAndTiming.computeChunkTiming(run, chunk.getPauseTimeMillis(), branchStartNode, endNode,
                        chunk.getNodeAfter());
                if(endNode instanceof StepAtomNode){
                    if(PipelineNodeUtil.isPausedForInputStep((StepAtomNode) endNode, inputAction)) {
                        status = new NodeRunStatus(BlueRun.BlueRunResult.UNKNOWN, BlueRun.BlueRunState.PAUSED);
                    }else{
                        status = new NodeRunStatus(endNode);
                    }
                }else {
                    GenericStatus genericStatus = StatusAndTiming.computeChunkStatus(run,
                            parallelStartNode, branchStartNode, endNode, parallelEnd);
                    status = new NodeRunStatus(genericStatus);
                }
            }else{
                times = new TimingInfo(TimingAction.getStartTime(branchStartNode)+System.currentTimeMillis(),
                        chunk.getPauseTimeMillis(),
                        TimingAction.getStartTime(branchStartNode));
                status = new NodeRunStatus(BlueRun.BlueRunResult.UNKNOWN, BlueRun.BlueRunState.RUNNING);
            }

            FlowNodeWrapper branch = new FlowNodeWrapper(branchStartNode, status, times, run);

            if(nextStage!=null) {
                branch.addEdge(nextStage.getId());
            }
            parallelBranches.push(branch);
        }

        FlowNodeWrapper[] sortedBranches = parallelBranches.toArray(new FlowNodeWrapper[parallelBranches.size()]);
        Arrays.sort(sortedBranches, new Comparator<FlowNodeWrapper>() {
            @Override
            public int compare(FlowNodeWrapper o1, FlowNodeWrapper o2) {
                return o1.getDisplayName().compareTo(o2.getDisplayName());
            }
        });

        parallelBranches.clear();
        for(int i=0; i< sortedBranches.length; i++){
            parallelBranches.push(sortedBranches[i]);
        }
        for(FlowNodeWrapper p:parallelBranches){
            nodes.push(p);
            nodeMap.put(p.getId(), p);
        }
        this.parallelEnd = null;
    }

    @Override
    public void parallelEnd(@Nonnull FlowNode parallelStartNode, @Nonnull FlowNode parallelEndNode, @Nonnull ForkScanner scanner) {
        if(isNodeVisitorDumpEnabled) {
            dump(String.format("parallelEnd=> id: %s, name: %s, function: %s", parallelEndNode.getId(),
                    parallelEndNode.getDisplayName(), parallelEndNode.getDisplayFunctionName()));
            if(parallelEndNode instanceof StepEndNode){
                dump(String.format("parallelEnd=> id: %s, StartNode: %s, name: %s, function: %s", parallelEndNode.getId(),
                        ((StepEndNode) parallelEndNode).getStartNode().getId(),((StepEndNode) parallelEndNode).getStartNode().getDisplayName(), ((StepEndNode) parallelEndNode).getStartNode().getDisplayFunctionName()));
            }
        }


        this.parallelEnd = parallelEndNode;
    }

    @Override
    public void parallelBranchStart(@Nonnull FlowNode parallelStartNode, @Nonnull FlowNode branchStartNode, @Nonnull ForkScanner scanner) {
        if(isNodeVisitorDumpEnabled)
            dump(String.format("parallelBranchStart=> id: %s, name: %s, function: %s", branchStartNode.getId(),
                    branchStartNode.getDisplayName(), branchStartNode.getDisplayFunctionName()));

        nestedbranches.push(branchStartNode);
    }

    @Override
    public void parallelBranchEnd(@Nonnull FlowNode parallelStartNode, @Nonnull FlowNode branchEndNode, @Nonnull ForkScanner scanner) {
        if(isNodeVisitorDumpEnabled) {
            dump(String.format("parallelBranchEnd=> id: %s, name: %s, function: %s, type: %s", branchEndNode.getId(),
                    branchEndNode.getDisplayName(), branchEndNode.getDisplayFunctionName(), branchEndNode.getClass()));
            if(branchEndNode instanceof StepEndNode){
                dump(String.format("parallelBranchEnd=> id: %s, StartNode: %s, name: %s, function: %s", branchEndNode.getId(),
                        ((StepEndNode) branchEndNode).getStartNode().getId(),((StepEndNode) branchEndNode).getStartNode().getDisplayName(),
                        ((StepEndNode) branchEndNode).getStartNode().getDisplayFunctionName()));
            }

        }
        parallelBranchEndNodes.add(branchEndNode);
    }

    // This gets triggered on encountering a new chunk (stage or branch)
    @Override
    protected void handleChunkDone(@Nonnull MemoryFlowChunk chunk) {
        if(isNodeVisitorDumpEnabled)
            dump(String.format("handleChunkDone=> id: %s, name: %s, function: %s", chunk.getFirstNode().getId(),
                    chunk.getFirstNode().getDisplayName(), chunk.getFirstNode().getDisplayFunctionName()));

        if(PipelineNodeUtil.isSyntheticStage(chunk.getFirstNode())){
            return;
        }

        if(!nestedStages.empty()){
            nestedStages.pop(); //we throw away nested stages
            if(!nestedStages.empty()){ //there is still a nested stage, return
                return;
            }
        }

        TimingInfo times = null;

        //TODO: remove chunk.getLastNode() != null check based on how JENKINS-40200 gets resolved
        if (firstExecuted != null && chunk.getLastNode() != null) {
            times = StatusAndTiming.computeChunkTiming(run, chunk.getPauseTimeMillis(), firstExecuted, chunk.getLastNode(), chunk.getNodeAfter());
        }

        if(times == null){
            times = new TimingInfo();
        }

        NodeRunStatus status;
        boolean skippedStage = PipelineNodeUtil.isSkippedStage(chunk.getFirstNode());
        if(skippedStage){
            status = new NodeRunStatus(BlueRun.BlueRunResult.NOT_BUILT, BlueRun.BlueRunState.SKIPPED);
        }else if (firstExecuted == null) {
            status = new NodeRunStatus(GenericStatus.NOT_EXECUTED);
        }else if(chunk.getLastNode() != null){
            status = new NodeRunStatus(StatusAndTiming
                    .computeChunkStatus(run, chunk.getNodeBefore(),
                            firstExecuted, chunk.getLastNode(), chunk.getNodeAfter()));
        }else{
            status = new NodeRunStatus(firstExecuted);
        }

        if (!pendingInputSteps.isEmpty()) {
            status = new NodeRunStatus(BlueRun.BlueRunResult.UNKNOWN, BlueRun.BlueRunState.PAUSED);
        }
        FlowNodeWrapper stage = new FlowNodeWrapper(chunk.getFirstNode(),
                status, times, run);

        try {
            String cause = PipelineNodeUtil.getCauseOfBlockage(stage.getNode(), agentNode, run);
            stage.setCauseOfFailure(cause);
        } catch (IOException | InterruptedException e) {
            //log the error but don't fail. This is better as in worst case all we will lose is blockage cause of a node.
            logger.error(String.format("Error trying to get blockage status of pipeline: %s, runId: %s node block: %s. %s"
                    ,run.getParent().getFullName(), run.getId(), agentNode, e.getMessage()), e);
        }
        nodes.push(stage);
        nodeMap.put(stage.getId(), stage);
        if(!skippedStage && !parallelBranches.isEmpty()){
            Iterator<FlowNodeWrapper> branches = parallelBranches.descendingIterator();
            while(branches.hasNext()){
                FlowNodeWrapper p = branches.next();
                p.addParent(stage);
                stage.addEdge(p.getId());
            }
        }else{
            if(nextStage != null) {
                nextStage.addParent(stage);
                stage.addEdge(nextStage.getId());
            }
        }
        parallelBranches.clear();
        this.nextStage = stage;
    }

    @Override
    protected void resetChunk(@Nonnull MemoryFlowChunk chunk) {
        super.resetChunk(chunk);
        firstExecuted = null;
        pendingInputSteps.clear();
    }

    @Override
    public void atomNode(@CheckForNull FlowNode before, @Nonnull FlowNode atomNode,
                         @CheckForNull FlowNode after, @Nonnull ForkScanner scan) {
        if(isNodeVisitorDumpEnabled)
            dump(String.format("atomNode=> id: %s, name: %s, function: %s, type: %s", atomNode.getId(),
                    atomNode.getDisplayName(), atomNode.getDisplayFunctionName(), atomNode.getClass()));

        if (NotExecutedNodeAction.isExecuted(atomNode)) {
            firstExecuted = atomNode;
        }
        long pause = PauseAction.getPauseDuration(atomNode);
        chunk.setPauseTimeMillis(chunk.getPauseTimeMillis()+pause);

        if(atomNode instanceof StepAtomNode
                && PipelineNodeUtil.isPausedForInputStep((StepAtomNode) atomNode, inputAction)){
            pendingInputSteps.add(atomNode);
        }
    }

    private void dump(String str){
        System.out.println(str);
    }

    @Override
    public List<FlowNodeWrapper> getPipelineNodes() {
        return new ArrayList<>(nodes);
    }

    @Override
    public List<BluePipelineNode> getPipelineNodes(Link parent) {
        List<BluePipelineNode> nodes = new ArrayList<>();
        for(FlowNodeWrapper n: this.nodes){
            nodes.add(new PipelineNodeImpl(n,parent, run));
        }
        return nodes;
    }

    @Override
    public List<BluePipelineStep> getPipelineNodeSteps(final String nodeId, Link parent) {
        DepthFirstScanner depthFirstScanner = new DepthFirstScanner();
        if(run.getExecution() == null){
            logger.debug(String.format("Pipeline %s, runid %s  has null execution", run.getParent().getName(), run.getId()));
            return Collections.emptyList();
        }
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
        if(run.getExecution() == null){
            return Collections.emptyList();
        }
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
        if(run.getExecution() == null){
            return null;
        }
        PipelineStepVisitor visitor = new PipelineStepVisitor(run, null);
        ForkScanner.visitSimpleChunks(run.getExecution().getCurrentHeads(), visitor, new StageChunkFinder());
        FlowNodeWrapper node = visitor.getStep(id);
        if( node == null){
            return null;
        }
        return new PipelineStepImpl(node, parent);
    }

    @Override
    public List<BluePipelineNode> union(List<FlowNodeWrapper> that, Link parent) {
        List<FlowNodeWrapper> currentNodes = new ArrayList<>(nodes);
        int currentNodeSize = nodes.size();
        int futureNodeSize = that.size();
        if(currentNodeSize < futureNodeSize){
            for(int i=currentNodeSize; i < futureNodeSize; i++){
                FlowNodeWrapper futureNode = that.get(i);

                //stitch future nodes to last nodes of current incomplete heads
                if(currentNodeSize>0 && i == currentNodeSize){
                    FlowNodeWrapper latestNode = currentNodes.get(i-1);
                    if(latestNode.type == FlowNodeWrapper.NodeType.STAGE){
                        latestNode.addEdge(futureNode.getId());
                    }else if(latestNode.type == FlowNodeWrapper.NodeType.PARALLEL){
                        //get stage of this parallel
                        FlowNodeWrapper stage = latestNode.getFirstParent();
                        if(stage != null){
                            //Add future node as edge to all edges of last stage
                            for(String id:stage.edges){
                                FlowNodeWrapper node = nodeMap.get(id);
                                if(node != null) {
                                    node.addEdge(futureNode.getId());
                                }
                            }
                        }
                    }
                }
                FlowNodeWrapper n = new FlowNodeWrapper(futureNode.getNode(),
                        new NodeRunStatus(null,null),
                        new TimingInfo(), run);
                n.addEdges(futureNode.edges);
                n.addParents(futureNode.getParents());
                currentNodes.add(n);
            }
        }
        List<BluePipelineNode> newNodes = new ArrayList<>();
        for(FlowNodeWrapper n: currentNodes){
            newNodes.add(new PipelineNodeImpl(n,parent,run));
        }
        return newNodes;
    }
}
