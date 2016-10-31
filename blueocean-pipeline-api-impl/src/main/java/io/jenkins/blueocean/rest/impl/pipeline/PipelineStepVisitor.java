package io.jenkins.blueocean.rest.impl.pipeline;

import org.jenkinsci.plugins.workflow.cps.nodes.StepAtomNode;
import org.jenkinsci.plugins.workflow.cps.nodes.StepEndNode;
import org.jenkinsci.plugins.workflow.graph.BlockEndNode;
import org.jenkinsci.plugins.workflow.graph.FlowNode;
import org.jenkinsci.plugins.workflow.graphanalysis.ForkScanner;
import org.jenkinsci.plugins.workflow.graphanalysis.MemoryFlowChunk;
import org.jenkinsci.plugins.workflow.graphanalysis.StandardChunkVisitor;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;
import org.jenkinsci.plugins.workflow.pipelinegraphanalysis.StatusAndTiming;
import org.jenkinsci.plugins.workflow.pipelinegraphanalysis.TimingInfo;
import org.jenkinsci.plugins.workflow.support.actions.PauseAction;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Gives steps inside
 *
 * - Stage boundary: Stage boundary ends where another another stage start or this stage block ends
 * - branch boundary: branch block boundary
 *
 * @author Vivek Pandey
 */
public class PipelineStepVisitor extends StandardChunkVisitor {
    private final FlowNode node;
    private final WorkflowRun run;

    private final ArrayDeque<FlowNodeWrapper> steps = new ArrayDeque<>();

    private final Map<String,FlowNodeWrapper> stepMap = new HashMap<>();

    private boolean stepCollectionCompleted = false;

    private boolean inStageScope;

    public PipelineStepVisitor(WorkflowRun run, @Nullable final FlowNode node) {
        this.node = node;
        this.run = run;
    }

    @Override
    public void parallelBranchStart(@Nonnull FlowNode parallelStartNode, @Nonnull FlowNode branchStartNode, @Nonnull ForkScanner scanner) {
        if(stepCollectionCompleted){ //skip
            return;
        }
        if(node != null && branchStartNode.equals(node)){
            stepCollectionCompleted = true;
        }else if(node != null && PipelineNodeUtil.isParallelBranch(node) && !branchStartNode.equals(node)){
            resetSteps();
        }
    }


    @Override
    public void parallelBranchEnd(@Nonnull FlowNode parallelStartNode, @Nonnull FlowNode branchEndNode, @Nonnull ForkScanner scanner) {
        if(!stepCollectionCompleted && node != null && PipelineNodeUtil.isParallelBranch(node) && branchEndNode instanceof StepEndNode){
            resetSteps();
        }
    }

    @Override
    public void chunkEnd(@Nonnull FlowNode endNode, @CheckForNull FlowNode afterChunk, @Nonnull ForkScanner scanner) {
        super.chunkEnd(endNode, afterChunk, scanner);
        if(node!= null && endNode instanceof StepEndNode && ((StepEndNode)endNode).getStartNode().equals(node)){
            this.stepCollectionCompleted = false;
            this.inStageScope = true;
        }
        // if we're using marker-based (and not block-scoped) stages, add the last node as part of its contents
        if (!(endNode instanceof BlockEndNode)) {
            atomNode(null, endNode, afterChunk, scanner);
        }
    }

    @Override
    protected void handleChunkDone(@Nonnull MemoryFlowChunk chunk) {
        if(stepCollectionCompleted){ //if its completed no further action
            return;
        }

        if(node != null && chunk.getFirstNode().equals(node)){
            stepCollectionCompleted = true;
            inStageScope = false;
        }if(node != null && PipelineNodeUtil.isStage(node) && !inStageScope && !chunk.getFirstNode().equals(node)){
            resetSteps();
        }
    }

    @Override
    public void atomNode(@CheckForNull FlowNode before, @Nonnull FlowNode atomNode, @CheckForNull FlowNode after, @Nonnull ForkScanner scan) {
        if(stepCollectionCompleted){
            return;
        }

        if(atomNode instanceof StepAtomNode) {
            if(stepMap.get(atomNode.getId()) != null){ // if already added then skip it
                return;
            }
            long pause = PauseAction.getPauseDuration(atomNode);
            chunk.setPauseTimeMillis(chunk.getPauseTimeMillis()+pause);

            TimingInfo times = StatusAndTiming.computeChunkTiming(run, pause, atomNode, atomNode, after); // TODO pipeline graph analysis adds this to TimingInfo

            if(times == null){
                times = new TimingInfo();
            }

            FlowNodeWrapper node = new FlowNodeWrapper(atomNode, new NodeRunStatus(atomNode), times);
            steps.push(node);
            stepMap.put(node.getId(), node);
        }
    }

    public List<FlowNodeWrapper> getSteps(){
        return new ArrayList<>(steps);
    }

    public FlowNodeWrapper getStep(String id){
        return stepMap.get(id);
    }

    private void resetSteps(){
        steps.clear();
        stepMap.clear();
    }

}
