package io.jenkins.blueocean.rest.impl.pipeline;

import com.google.common.base.Predicate;
import io.jenkins.blueocean.rest.hal.Link;
import io.jenkins.blueocean.rest.model.BluePipelineNode;
import io.jenkins.blueocean.rest.model.BluePipelineStep;
import org.jenkinsci.plugins.workflow.actions.NotExecutedNodeAction;
import org.jenkinsci.plugins.workflow.cps.nodes.StepAtomNode;
import org.jenkinsci.plugins.workflow.cps.nodes.StepEndNode;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Vivek Pandey
 */
public class PipelineNodeGraphVisitor extends StandardChunkVisitor implements NodeGraphBuilder{
    private final WorkflowRun run;

    public final Map<FlowNodeWrapper, List<FlowNodeWrapper>> parentToChildrenMap = new LinkedHashMap<>();

    private final ArrayDeque<FlowNodeWrapper> parallelBranches = new ArrayDeque<>();

    public final ArrayDeque<FlowNodeWrapper> nodes = new ArrayDeque<>();

    private FlowNode firstExecuted = null;

    private FlowNodeWrapper nextStage;

    private FlowNode branchEnd;

    private FlowNode parallelEnd;

    public final Map<String, FlowNodeWrapper> nodeMap = new HashMap<>();

    private static final Logger logger = LoggerFactory.getLogger(PipelineNodeGraphVisitor.class);

    private static final boolean isNodeVisitorDumpEnabled = Boolean.getBoolean("NODE_DUMP_ENABLED");

    public PipelineNodeGraphVisitor(WorkflowRun run) {
        this.run = run;
        if(run.getExecution()!=null) {
            ForkScanner.visitSimpleChunks(run.getExecution().getCurrentHeads(), this, new StageChunkFinder());
        }
    }

    @Override
    public void chunkStart(@Nonnull FlowNode startNode, @CheckForNull FlowNode beforeBlock, @Nonnull ForkScanner scanner) {
        super.chunkStart(startNode, beforeBlock, scanner);
        if(isNested()){
            return;
        }

        if (NotExecutedNodeAction.isExecuted(startNode)) {
            firstExecuted = startNode;
        }
        if(isNodeVisitorDumpEnabled)
            dump(String.format("chunkStart=> id: %s, name: %s, function: %s", startNode.getId(),
                startNode.getDisplayName(), startNode.getDisplayFunctionName()));
    }

    @Override
    public void chunkEnd(@Nonnull FlowNode endNode, @CheckForNull FlowNode afterBlock, @Nonnull ForkScanner scanner) {
        super.chunkEnd(endNode, afterBlock, scanner);

        if(isNodeVisitorDumpEnabled)
            dump(String.format("chunkEnd=> id: %s, name: %s, function: %s, type:%s", endNode.getId(),
                endNode.getDisplayName(), endNode.getDisplayFunctionName(), endNode.getClass()));
        if(isNested()){
            return;
        }

        if(isNodeVisitorDumpEnabled && endNode instanceof StepEndNode){
            dump("\tStartNode: "+((StepEndNode) endNode).getStartNode());
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
        for(FlowNodeWrapper p:parallelBranches){
            nodes.push(p);
            nodeMap.put(p.getId(), p);
        }
        this.parallelEnd = null;

    }

    @Override
    public void parallelEnd(@Nonnull FlowNode parallelStartNode, @Nonnull FlowNode parallelEndNode, @Nonnull ForkScanner scanner) {
        if(isNodeVisitorDumpEnabled)
            dump(String.format("parallelEnd=> id: %s, name: %s, function: %s", parallelEndNode.getId(),
                parallelEndNode.getDisplayName(), parallelEndNode.getDisplayFunctionName()));

        this.parallelEnd = parallelEndNode;
    }

    @Override
    public void parallelBranchStart(@Nonnull FlowNode parallelStartNode, @Nonnull FlowNode branchStartNode, @Nonnull ForkScanner scanner) {
        if(isNodeVisitorDumpEnabled)
            dump(String.format("parallelBranchStart=> id: %s, name: %s, function: %s", branchStartNode.getId(),
                branchStartNode.getDisplayName(), branchStartNode.getDisplayFunctionName()));

        TimingInfo times = StatusAndTiming.computeChunkTiming(run, chunk.getPauseTimeMillis(), branchStartNode, branchEnd,
            chunk.getNodeAfter());

        if(times == null){
            times = new TimingInfo();
        }

        GenericStatus status = StatusAndTiming.computeChunkStatus(run,
            parallelStartNode, branchStartNode, branchEnd, parallelEnd);

        FlowNodeWrapper branch = new FlowNodeWrapper(branchStartNode,
            new NodeRunStatus(status), times);

        if(nextStage!=null) {
            branch.addEdge(nextStage.getId());
        }
        parallelBranches.push(branch);
        this.branchEnd = null;
    }

    @Override
    public void parallelBranchEnd(@Nonnull FlowNode parallelStartNode, @Nonnull FlowNode branchEndNode, @Nonnull ForkScanner scanner) {
        if(isNodeVisitorDumpEnabled)
            dump(String.format("parallelBranchEnd=> id: %s, name: %s, function: %s, type: %s", branchEndNode.getId(),
                branchEndNode.getDisplayName(), branchEndNode.getDisplayFunctionName(), branchEndNode.getClass()));
        this.branchEnd = branchEndNode;
    }

    private boolean isNested(){
        return branchEnd!=null;
    }

    // This gets triggered on encountering a new chunk (stage or branch)
    @Override
    protected void handleChunkDone(@Nonnull MemoryFlowChunk chunk) {
        if(isNodeVisitorDumpEnabled)
            dump(String.format("handleChunkDone=> id: %s, name: %s, function: %s", chunk.getFirstNode().getId(),
                chunk.getFirstNode().getDisplayName(), chunk.getFirstNode().getDisplayFunctionName()));
        if(isNested()){
            return;
        }
        TimingInfo times = StatusAndTiming.computeChunkTiming(run, chunk.getPauseTimeMillis(), firstExecuted, chunk.getLastNode(), chunk.getNodeAfter());

        if(times == null){
            times = new TimingInfo();
        }

        GenericStatus status = (firstExecuted == null) ? GenericStatus.NOT_EXECUTED :StatusAndTiming
            .computeChunkStatus(run, chunk.getNodeBefore(), firstExecuted, chunk.getLastNode(), chunk.getNodeAfter());


        FlowNodeWrapper stage = new FlowNodeWrapper(chunk.getFirstNode(),
            new NodeRunStatus(status), times);

        nodes.push(stage);
        nodeMap.put(stage.getId(), stage);
        if(!parallelBranches.isEmpty()){
            for(FlowNodeWrapper p:parallelBranches){
                p.addParent(stage);
                stage.addEdge(p.getId());
            }
            parallelBranches.clear();
        }else{
            if(nextStage != null) {
                nextStage.addParent(stage);
                stage.addEdge(nextStage.getId());
            }
        }
        this.nextStage = stage;
    }

    @Override
    protected void resetChunk(@Nonnull MemoryFlowChunk chunk) {
        super.resetChunk(chunk);
        firstExecuted = null;
    }

    @Override
    public void atomNode(@CheckForNull FlowNode before, @Nonnull FlowNode atomNode, @CheckForNull FlowNode after, @Nonnull ForkScanner scan) {
        if(isNodeVisitorDumpEnabled)
            dump(String.format("atomNode=> id: %s, name: %s, function: %s, type: %s", atomNode.getId(),
                atomNode.getDisplayName(), atomNode.getDisplayFunctionName(), atomNode.getClass()));

        if (NotExecutedNodeAction.isExecuted(atomNode)) {
            firstExecuted = atomNode;
        }
        long pause = PauseAction.getPauseDuration(atomNode);
        chunk.setPauseTimeMillis(chunk.getPauseTimeMillis()+pause);

        if(atomNode instanceof StepAtomNode) {
            TimingInfo times = StatusAndTiming.computeChunkTiming(run, pause, atomNode, atomNode, after); // TODO pipeline graph analysis adds this to TimingInfo

            if(times == null){
                times = new TimingInfo();
            }
            GenericStatus status = StatusAndTiming.computeChunkStatus(run, before, atomNode, atomNode, after);
            if (status == null) {
                status = GenericStatus.NOT_EXECUTED;
            }

            FlowNodeWrapper node = new FlowNodeWrapper(atomNode, new NodeRunStatus(status), times);
            nodeMap.put(node.getId(), node);
        }
    }

    private void dump(String str){
        logger.debug(str);
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
                    new TimingInfo());
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
