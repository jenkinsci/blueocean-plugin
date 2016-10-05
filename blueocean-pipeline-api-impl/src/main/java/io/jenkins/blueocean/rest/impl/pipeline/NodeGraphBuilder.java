package io.jenkins.blueocean.rest.impl.pipeline;

import io.jenkins.blueocean.rest.hal.Link;
import io.jenkins.blueocean.rest.model.BluePipelineNode;
import io.jenkins.blueocean.rest.model.BluePipelineStep;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;

import java.util.List;

/**
 * @author Vivek Pandey
 */
public interface NodeGraphBuilder {
    List<FlowNodeWrapper> getPipelineNodes();
    List<BluePipelineNode> getPipelineNodes(Link parent);
    List<BluePipelineStep> getPipelineNodeSteps(String nodeId, Link parent);
    List<BluePipelineStep> getPipelineNodeSteps(Link parent);
    BluePipelineStep getPipelineNodeStep(String id, Link parent);

    List<BluePipelineNode> union(List<FlowNodeWrapper> that, Link parent);

    final class NodeGraphBuilderFactory{
        public static final NodeGraphBuilder getInstance(WorkflowRun run){
            return Boolean.getBoolean("LEGACY_PIPELINE_NODE_PARSER")
                ? new PipelineNodeGraphBuilder(run)
                : new PipelineNodeGraphVisitor(run);
        }
    }
}
