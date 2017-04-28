package io.jenkins.blueocean.rest.impl.pipeline;

import io.jenkins.blueocean.rest.hal.Link;
import io.jenkins.blueocean.rest.model.BluePipelineNode;
import io.jenkins.blueocean.rest.model.BluePipelineStep;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;

import java.util.List;

/**
 * Pipeline node graph builder
 *
 * @author Vivek Pandey
 */
public interface NodeGraphBuilder {
    /** Gives all pipeline nodes DAG graph */
    List<FlowNodeWrapper> getPipelineNodes();

    /** Gives DAG graph as list of {@link io.jenkins.blueocean.rest.model.BluePipelineNode} */
    List<BluePipelineNode> getPipelineNodes(Link parent);

    /** Gives all the steps inside given nodeId */
    List<BluePipelineStep> getPipelineNodeSteps(String nodeId, Link parent);

    /** Gives all the steps in this pipeline */
    List<BluePipelineStep> getPipelineNodeSteps(Link parent);

    /** Give the step for given id */
    BluePipelineStep getPipelineNodeStep(String id, Link parent);

    /** Create union of last successful run and this partial run */
    List<BluePipelineNode> union(List<FlowNodeWrapper> lastBuildGraph, Link parent);

    /** Factory to give pipeline DAG builder */
    // At this point we are not exposing NodeGraphBuilder as ExtensionPoint, its more of convenience to allow us
    // to use alternative implementation in future
    final class NodeGraphBuilderFactory{
        public static NodeGraphBuilder getInstance(WorkflowRun run){
            return new PipelineNodeGraphVisitor(run);
        }
    }
}
