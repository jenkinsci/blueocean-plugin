package io.jenkins.blueocean.rest.impl.pipeline;

import com.google.common.base.Predicate;
import org.jenkinsci.plugins.workflow.graph.FlowNode;
import org.jenkinsci.plugins.workflow.graphanalysis.DepthFirstScanner;
import org.jenkinsci.plugins.workflow.graphanalysis.MemoryFlowChunk;
import org.jenkinsci.plugins.workflow.graphanalysis.StandardChunkVisitor;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * @author Vivek Pandey
 */
public class PipelineStepVisitor extends StandardChunkVisitor {
    private final FlowNode node;

    private final FlowNode endNode;
    public PipelineStepVisitor(WorkflowRun run, FlowNode node) {
        //Get the bound
        this.node = node;

        DepthFirstScanner depthFirstScanner = new DepthFirstScanner();
        FlowNode n = depthFirstScanner.findFirstMatch(run.getExecution().getCurrentHeads(), new Predicate<FlowNode>() {
            @Override
            public boolean apply(@Nullable FlowNode input) {
                if(input instanceof )
            }
        })
    }

    @Override
    protected void handleChunkDone(@Nonnull MemoryFlowChunk chunk) {
        if(chunk.getFirstNode().equals(node)){
            this.endNode = chunk.getNodeAfter();
        }

    }
}
