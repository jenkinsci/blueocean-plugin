package io.jenkins.blueocean.rest.impl.pipeline;

import io.jenkins.blueocean.rest.hal.Link;
import io.jenkins.blueocean.rest.model.BlueActionProxy;
import io.jenkins.blueocean.rest.model.BluePipelineStep;
import io.jenkins.blueocean.rest.model.BlueRun;
import io.jenkins.blueocean.service.embedded.rest.LogResource;
import org.jenkinsci.plugins.workflow.actions.LogAction;

import java.util.Collection;
import java.util.Date;

/**
 * @author Vivek Pandey
 */
public class PipelineStepImpl extends BluePipelineStep {
    private final FlowNodeWrapper node;
    private final Link self;

    public PipelineStepImpl(FlowNodeWrapper node, Link parent) {
        assert node != null;
        this.self = parent.rel(node.getId());
        this.node = node;
    }

    @Override
    public String getId() {
        return node.getId();
    }

    @Override
    public String getDisplayName() {
        return node.getNode().getDisplayName();
    }

    @Override
    public BlueRun.BlueRunResult getResult() {
        return node.getStatus().getResult();
    }

    @Override
    public BlueRun.BlueRunState getStateObj() {
        return node.getStatus().getState();
    }

    @Override
    public Date getStartTime() {
        return new Date(node.getTiming().getStartTimeMillis());
    }

    @Override
    public Long getDurationInMillis() {
        return node.getTiming().getTotalDurationMillis();
    }

    @Override
    public Object getLog() {

        if(PipelineNodeUtil.isLoggable.apply(node.getNode())){
            return new LogResource(node.getNode().getAction(LogAction.class).getLogText());
        }
        return null;
    }

    @Override
    public Collection<BlueActionProxy> getActions() {
        return PipelineImpl.getActionProxies(node.getNode().getActions(), this);
    }

    @Override
    public Link getLink() {
        return self;
    }
}
