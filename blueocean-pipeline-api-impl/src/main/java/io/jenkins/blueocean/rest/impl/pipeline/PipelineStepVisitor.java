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

    public PipelineStepVisitor(WorkflowRun run, @Nullable final FlowNode node) {
        //Get the bound
        this.node = node;
        this.run = run;


//
//        DepthFirstScanner depthFirstScanner = new DepthFirstScanner();
//        //If blocked scope, get the end node
//        FlowNode n = depthFirstScanner.findFirstMatch(run.getExecution().getCurrentHeads(), new Predicate<FlowNode>() {
//            @Override
//            public boolean apply(@Nullable FlowNode input) {
//                if(input instanceof StepEndNode && ((StepEndNode)input).getStartNode().equals(node)){
//                    return true;
//                }
//                return false;
//            }
//        });
//
//        //If legacy stage, then get the next stage or end of pipeline
//        n = depthFirstScanner.findFirstMatch(run.getExecution().getCurrentHeads(), new Predicate<FlowNode>() {
//            @Override
//            public boolean apply(@Nullable FlowNode input) {
//                if(PipelineNodeUtil.isStage(input) && input.getId() > ){
//
//                }
//            }
//        })

    }

    @Override
    public void parallelBranchEnd(@Nonnull FlowNode parallelStartNode, @Nonnull FlowNode branchEndNode, @Nonnull ForkScanner scanner) {
        if(stepCollectionCompleted){ //skip
            return;
        }
        if(branchEndNode instanceof StepEndNode && ((StepEndNode) branchEndNode).getStartNode().equals(node)){
            stepCollectionCompleted = true;
        }else{
            // if given node is parallel and if its not ours then we clear the previously collected steps
            if(node != null && PipelineNodeUtil.isParallelBranch(node)) {
                steps.clear();
            }
        }
    }

    @Override
    protected void handleChunkDone(@Nonnull MemoryFlowChunk chunk) {
        if(stepCollectionCompleted){ //if its completed no further action
            return;
        }

        if(node != null && chunk.getFirstNode().equals(node)){
            stepCollectionCompleted = true;
        }else{
            // if given node is a stage and if its not ours then we clear the previously collected steps
            if(node != null && PipelineNodeUtil.isStage(node)) {
                steps.clear();
            }
        }
    }

    @Override
    public void atomNode(@CheckForNull FlowNode before, @Nonnull FlowNode atomNode, @CheckForNull FlowNode after, @Nonnull ForkScanner scan) {
        if(stepCollectionCompleted){ //no further action if already completed
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

            FlowNodeWrapper node = new FlowNodeWrapper(atomNode, new PipelineNodeGraphBuilder.NodeRunStatus(status), times);
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
