package io.jenkins.blueocean.rest.impl.pipeline;

import org.jenkinsci.plugins.workflow.actions.NotExecutedNodeAction;
import org.jenkinsci.plugins.workflow.cps.nodes.StepAtomNode;
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

    private final ArrayDeque<FlowNodeWrapper> steps = new ArrayDeque<>();

    private final List<FlowNodeWrapper> parallelBranches = new ArrayList<>();

    public final ArrayDeque<FlowNodeWrapper> nodes = new ArrayDeque<>();

    private FlowNode firstExecuted = null;

    private FlowNodeWrapper nextStage;

    private FlowNode branchEnd;

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
        System.out.println(String.format("chunkEnd=> id: %s, name: %s, function: %s", startNode.getId(),
            startNode.getDisplayName(), startNode.getDisplayFunctionName()));
    }

    @Override
    public void chunkEnd(@Nonnull FlowNode endNode, @CheckForNull FlowNode afterBlock, @Nonnull ForkScanner scanner) {
        super.chunkEnd(endNode, afterBlock, scanner);

        System.out.println(String.format("chunkEnd=> id: %s, name: %s, function: %s", endNode.getId(),
            endNode.getDisplayName(), endNode.getDisplayFunctionName()));

        firstExecuted = null;

        // if we're using marker-based (and not block-scoped) stages, add the last node as part of its contents
        if (!(endNode instanceof BlockEndNode)) {
            atomNode(null, endNode, afterBlock, scanner);
        }
    }

    @Override
    public void parallelStart(@Nonnull FlowNode parallelStartNode, @Nonnull FlowNode branchNode, @Nonnull ForkScanner scanner) {
        super.parallelStart(parallelStartNode, branchNode, scanner);
        System.out.println(String.format("parallelStart=> id: %s, name: %s, function: %s", parallelStartNode.getId(),
            parallelStartNode.getDisplayName(), parallelStartNode.getDisplayFunctionName()));
        System.out.println(String.format("\tbranch=> id: %s, name: %s, function: %s", branchNode.getId(),
            branchNode.getDisplayName(), branchNode.getDisplayFunctionName()));

        if(parallelBranches.size() > 0) {
            for (int i = parallelBranches.size()-1; i >= 0; i--) {
                FlowNodeWrapper p = parallelBranches.get(i);
                nodes.push(parallelBranches.get(i));
                nodeMap.put(p.getId(), p);
            }
        }
    }

    @Override
    public void parallelEnd(@Nonnull FlowNode parallelStartNode, @Nonnull FlowNode parallelEndNode, @Nonnull ForkScanner scanner) {
        super.parallelEnd(parallelStartNode, parallelEndNode, scanner);
        System.out.println(String.format("parallelEnd=> id: %s, name: %s, function: %s", parallelEndNode.getId(),
            parallelEndNode.getDisplayName(), parallelEndNode.getDisplayFunctionName()));

        parallelBranches.clear();
    }

    @Override
    public void parallelBranchStart(@Nonnull FlowNode parallelStartNode, @Nonnull FlowNode branchStartNode, @Nonnull ForkScanner scanner) {
        super.parallelBranchStart(parallelStartNode, branchStartNode, scanner);
        System.out.println(String.format("parallelBranchStart=> id: %s, name: %s, function: %s", branchStartNode.getId(),
            branchStartNode.getDisplayName(), branchStartNode.getDisplayFunctionName()));

        TimingInfo times = StatusAndTiming.computeChunkTiming(run, chunk.getPauseTimeMillis(), branchStartNode, branchEnd,
            chunk.getNodeAfter());

        if(times == null){
            times = new TimingInfo();
        }

        GenericStatus status = StatusAndTiming.computeChunkStatus(run,
            chunk.getNodeBefore(), branchStartNode, branchEnd, chunk.getNodeAfter());

        FlowNodeWrapper branch = new FlowNodeWrapper(branchStartNode,
            new PipelineNodeGraphBuilder.NodeRunStatus(status), times);

        branch.addEdge(nextStage.getId());
        parallelBranches.add(branch);
        this.branchEnd = null;

        for(FlowNodeWrapper n:steps) {
            branch.steps.add(n);
        }
        steps.clear();
    }

    @Override
    public void parallelBranchEnd(@Nonnull FlowNode parallelStartNode, @Nonnull FlowNode branchEndNode, @Nonnull ForkScanner scanner) {
        super.parallelBranchEnd(parallelStartNode, branchEndNode, scanner);
        System.out.println(String.format("parallelBranchEnd=> id: %s, name: %s, function: %s", branchEndNode.getId(),
            branchEndNode.getDisplayName(), branchEndNode.getDisplayFunctionName()));
        this.branchEnd = branchEndNode;
    }

    // This gets triggered on encountering a new chunk (stage or branch)
    @Override
    protected void handleChunkDone(@Nonnull MemoryFlowChunk chunk) {
        System.out.println(String.format("handleChunkDone=> id: %s, name: %s, function: %s", chunk.getFirstNode().getId(),
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
        System.out.println(String.format("atomNode=> id: %s, name: %s, function: %s, type: %s", atomNode.getId(),
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
            steps.push(node);
            nodeMap.put(node.getId(), node);
        }
    }

    public FlowNodeWrapper getStep(String id){
        return nodeMap.get(id);
    }

}
