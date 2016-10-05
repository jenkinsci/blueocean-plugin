package io.jenkins.blueocean.rest.impl.pipeline;

import org.jenkinsci.plugins.workflow.actions.NotExecutedNodeAction;
import org.jenkinsci.plugins.workflow.cps.nodes.StepAtomNode;
import org.jenkinsci.plugins.workflow.cps.nodes.StepEndNode;
import org.jenkinsci.plugins.workflow.graph.BlockEndNode;
import org.jenkinsci.plugins.workflow.graph.FlowNode;
import org.jenkinsci.plugins.workflow.graphanalysis.ForkScanner;
import org.jenkinsci.plugins.workflow.graphanalysis.MemoryFlowChunk;
import org.jenkinsci.plugins.workflow.graphanalysis.StandardChunkVisitor;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;
import org.jenkinsci.plugins.workflow.pipelinegraphanalysis.GenericStatus;
import org.jenkinsci.plugins.workflow.pipelinegraphanalysis.StatusAndTiming;
import org.jenkinsci.plugins.workflow.pipelinegraphanalysis.TimingInfo;
import org.jenkinsci.plugins.workflow.support.actions.PauseAction;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Vivek Pandey
 */
public class PipelineNodeGraphVisitor extends StandardChunkVisitor {
    private final WorkflowRun run;

    public final Map<FlowNodeWrapper, List<FlowNodeWrapper>> parentToChildrenMap = new LinkedHashMap<>();

    private final List<FlowNodeWrapper> parallelBranches = new ArrayList<>();

    public final ArrayDeque<FlowNodeWrapper> nodes = new ArrayDeque<>();

    private FlowNode firstExecuted = null;

    private FlowNodeWrapper nextStage;

    private FlowNode branchEnd;

    private FlowNode parallelEnd;

    public final Map<String, FlowNodeWrapper> nodeMap = new HashMap<>();


    public PipelineNodeGraphVisitor(WorkflowRun run) {
        this.run = run;
    }

    @Override
    public void chunkStart(@Nonnull FlowNode startNode, @CheckForNull FlowNode beforeBlock, @Nonnull ForkScanner scanner) {
        super.chunkStart(startNode, beforeBlock, scanner);
        if (NotExecutedNodeAction.isExecuted(startNode)) {
            firstExecuted = startNode;
        }
        dump(String.format("chunkStart=> id: %s, name: %s, function: %s", startNode.getId(),
            startNode.getDisplayName(), startNode.getDisplayFunctionName()));
    }

    @Override
    public void chunkEnd(@Nonnull FlowNode endNode, @CheckForNull FlowNode afterBlock, @Nonnull ForkScanner scanner) {
        super.chunkEnd(endNode, afterBlock, scanner);

        dump(String.format("chunkEnd=> id: %s, name: %s, function: %s, type:%s", endNode.getId(),
            endNode.getDisplayName(), endNode.getDisplayFunctionName(), endNode.getClass()));
        if(endNode instanceof StepEndNode){
            System.out.println("\tStartNode: "+((StepEndNode) endNode).getStartNode());
        }

        firstExecuted = null;

        // if we're using marker-based (and not block-scoped) stages, add the last node as part of its contents
        if (!(endNode instanceof BlockEndNode)) {
            atomNode(null, endNode, afterBlock, scanner);
        }
    }

    @Override
    public void parallelStart(@Nonnull FlowNode parallelStartNode, @Nonnull FlowNode branchNode, @Nonnull ForkScanner scanner) {
        super.parallelStart(parallelStartNode, branchNode, scanner);
        dump(String.format("parallelStart=> id: %s, name: %s, function: %s", parallelStartNode.getId(),
            parallelStartNode.getDisplayName(), parallelStartNode.getDisplayFunctionName()));
        dump(String.format("\tbranch=> id: %s, name: %s, function: %s", branchNode.getId(),
            branchNode.getDisplayName(), branchNode.getDisplayFunctionName()));

        for(FlowNodeWrapper p:parallelBranches){
            nodes.push(p);
            nodeMap.put(p.getId(), p);
        }
        this.parallelEnd = null;

    }

    @Override
    public void parallelEnd(@Nonnull FlowNode parallelStartNode, @Nonnull FlowNode parallelEndNode, @Nonnull ForkScanner scanner) {
        super.parallelEnd(parallelStartNode, parallelEndNode, scanner);
        dump(String.format("parallelEnd=> id: %s, name: %s, function: %s", parallelEndNode.getId(),
            parallelEndNode.getDisplayName(), parallelEndNode.getDisplayFunctionName()));

        this.parallelEnd = parallelEndNode;
    }

    @Override
    public void parallelBranchStart(@Nonnull FlowNode parallelStartNode, @Nonnull FlowNode branchStartNode, @Nonnull ForkScanner scanner) {
        super.parallelBranchStart(parallelStartNode, branchStartNode, scanner);
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
            new PipelineNodeGraphBuilder.NodeRunStatus(status), times);

        if(nextStage!=null) {
            branch.addEdge(nextStage.getId());
        }
        parallelBranches.add(branch);
        this.branchEnd = null;
    }

    @Override
    public void parallelBranchEnd(@Nonnull FlowNode parallelStartNode, @Nonnull FlowNode branchEndNode, @Nonnull ForkScanner scanner) {
        super.parallelBranchEnd(parallelStartNode, branchEndNode, scanner);
        dump(String.format("parallelBranchEnd=> id: %s, name: %s, function: %s, type: %s", branchEndNode.getId(),
            branchEndNode.getDisplayName(), branchEndNode.getDisplayFunctionName(), branchEndNode.getClass()));
        this.branchEnd = branchEndNode;
    }

    // This gets triggered on encountering a new chunk (stage or branch)
    @Override
    protected void handleChunkDone(@Nonnull MemoryFlowChunk chunk) {
        dump(String.format("handleChunkDone=> id: %s, name: %s, function: %s", chunk.getFirstNode().getId(),
            chunk.getFirstNode().getDisplayName(), chunk.getFirstNode().getDisplayFunctionName()));
        TimingInfo times = StatusAndTiming.computeChunkTiming(run, chunk.getPauseTimeMillis(), firstExecuted, chunk.getLastNode(), chunk.getNodeAfter());

        if(times == null){
            times = new TimingInfo();
        }

        GenericStatus status = (firstExecuted == null) ? GenericStatus.NOT_EXECUTED :StatusAndTiming
            .computeChunkStatus(run, chunk.getNodeBefore(), firstExecuted, chunk.getLastNode(), chunk.getNodeAfter());


        FlowNodeWrapper stage = new FlowNodeWrapper(chunk.getFirstNode(),
            new PipelineNodeGraphBuilder.NodeRunStatus(status), times);

        nodes.push(stage);
        nodeMap.put(stage.getId(), stage);
        if(!parallelBranches.isEmpty()){
            for(FlowNodeWrapper p:parallelBranches){
                stage.addEdge(p.getId());
            }
            parallelBranches.clear();
        }else{
            if(nextStage != null) {
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

            FlowNodeWrapper node = new FlowNodeWrapper(atomNode, new PipelineNodeGraphBuilder.NodeRunStatus(status), times);
            nodeMap.put(node.getId(), node);
        }
    }

    private void dump(String str){
//        System.out.println(str);
    }

}
