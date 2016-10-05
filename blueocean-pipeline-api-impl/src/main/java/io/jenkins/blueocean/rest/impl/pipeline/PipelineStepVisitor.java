package io.jenkins.blueocean.rest.impl.pipeline;

import org.jenkinsci.plugins.workflow.cps.nodes.StepAtomNode;
import org.jenkinsci.plugins.workflow.cps.nodes.StepEndNode;
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
import javax.annotation.Nullable;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Gives steps inside
 *
 * - Stage boudary: Stage boundry ends where another another stage start or this stage block ends
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
            steps.clear();
        }
    }


    @Override
    public void chunkEnd(@Nonnull FlowNode endNode, @CheckForNull FlowNode afterChunk, @Nonnull ForkScanner scanner) {
        super.chunkEnd(endNode, afterChunk, scanner);
        if(node!= null && endNode instanceof StepEndNode && ((StepEndNode)endNode).getStartNode().equals(node)){
            this.stepCollectionCompleted = false;
            this.inStageScope = true;
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
            steps.clear();
        }
    }

    @Override
    public void atomNode(@CheckForNull FlowNode before, @Nonnull FlowNode atomNode, @CheckForNull FlowNode after, @Nonnull ForkScanner scan) {
        if(stepCollectionCompleted){
            return;
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

}
