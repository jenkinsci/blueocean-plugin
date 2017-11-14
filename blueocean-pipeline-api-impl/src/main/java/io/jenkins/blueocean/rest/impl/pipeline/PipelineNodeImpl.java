package io.jenkins.blueocean.rest.impl.pipeline;

import com.google.common.base.Predicate;
import hudson.model.Action;
import io.jenkins.blueocean.rest.hal.Link;
import io.jenkins.blueocean.rest.model.BlueActionProxy;
import io.jenkins.blueocean.rest.model.BlueInputStep;
import io.jenkins.blueocean.rest.model.BluePipelineNode;
import io.jenkins.blueocean.rest.model.BluePipelineStep;
import io.jenkins.blueocean.rest.model.BluePipelineStepContainer;
import io.jenkins.blueocean.rest.model.BlueRun;
import io.jenkins.blueocean.service.embedded.rest.AbstractRunImpl;
import io.jenkins.blueocean.service.embedded.rest.ActionProxiesImpl;
import org.jenkinsci.plugins.workflow.actions.LogAction;
import org.jenkinsci.plugins.workflow.graph.FlowNode;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;
import org.kohsuke.stapler.HttpResponse;
import org.kohsuke.stapler.StaplerRequest;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import org.kohsuke.stapler.export.Exported;

/**
 * Implementation of {@link BluePipelineNode}.
 *
 * @author Vivek Pandey
 * @see FlowNode
 */
public class PipelineNodeImpl extends BluePipelineNode {
    private final FlowNodeWrapper node;
    private final List<Edge> edges;
    private final Long durationInMillis;
    private final NodeRunStatus status;
    private final Link self;
    private final WorkflowRun run;

    public PipelineNodeImpl(FlowNodeWrapper node, Link parentLink, WorkflowRun run) {
        this.node = node;
        this.run = run;
        this.edges = buildEdges(node.edges);
        this.status = node.getStatus();
        this.durationInMillis = node.getTiming().getTotalDurationMillis();
        this.self = parentLink.rel(node.getId());
    }

    @Override
    public String getId() {
        return node.getId();
    }

    @Override
    public String getDisplayName() {
        return PipelineNodeUtil.getDisplayName(node.getNode());
    }

    @Override
    public String getDisplayDescription() {
        return null;
    }

    @Override
    public BlueRun.BlueRunResult getResult() {
        return status.getResult();
    }

    @Override
    public BlueRun.BlueRunState getStateObj() {
        return status.getState();
    }

    @Override
    public Date getStartTime() {
        long nodeTime = node.getTiming().getStartTimeMillis();
        if(nodeTime == 0){
            return null;
        }
        return new Date(nodeTime);
    }

    public String getStartTimeString(){
        if(getStartTime() == null) {
            return null;
        }
        return AbstractRunImpl.DATE_FORMAT.print(getStartTime().getTime());
    }

    @Override
    public List<Edge> getEdges() {
        return edges;
    }

    @Override
    public Long getDurationInMillis() {
        return durationInMillis;
    }

    /**
     * Appended logs of steps.
     *
     * @see BluePipelineStep#getLog()
     */
    @Override
    public Object getLog() {
        return new NodeLogResource(this);
    }

    @Override
    public String getType(){
        return node.getType().name();
    }

    @Override
    public String getCauseOfBlockage() {
        return node.getCauseOfFailure();
    }

    @Override
    public BluePipelineStepContainer getSteps() {
        return new PipelineStepContainerImpl(node, self, run);
    }

    @Override
    public Link getLink() {
        return self;
    }

    @Override
    public Collection<BlueActionProxy> getActions() {
        return ActionProxiesImpl.getActionProxies(node.getNode().getActions(), new Predicate<Action>() {
            @Override
            public boolean apply(@Nullable Action input) {
                return input instanceof LogAction;
            }
        }, this);
    }

    @Override
    public BlueInputStep getInputStep() {
        return null;
    }

    @Override
    public HttpResponse submitInputStep(StaplerRequest request) {
        return null;
    }

    public static class EdgeImpl extends Edge{
        private final String id;
        private final String type;

        public EdgeImpl(FlowNodeWrapper edge) {
            this.id = edge.getId();
            this.type = edge.getType().name();
        }

        @Override
        public String getId() {
            return id;
        }

        @Exported
        public String getType() {
            return type;
        }
    }

    private List<Edge> buildEdges(List<FlowNodeWrapper> nodes){
        List<Edge> edges  = new ArrayList<>();
        if(!nodes.isEmpty()) {
            for (FlowNodeWrapper edge:nodes) {
                edges.add(new EdgeImpl(edge));
            }
        }
        return edges;
    }

    FlowNodeWrapper getFlowNodeWrapper(){
        return node;
    }

}
