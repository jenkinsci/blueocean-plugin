package io.jenkins.blueocean.service.embedded.rest;

import hudson.scm.ChangeLogSet;
import hudson.scm.ChangeLogSet.Entry;
import io.jenkins.blueocean.rest.hal.Link;
import io.jenkins.blueocean.rest.model.BluePipelineNodeContainer;
import io.jenkins.blueocean.rest.model.BluePipelineStep;
import io.jenkins.blueocean.rest.model.BlueRun;
import io.jenkins.blueocean.rest.model.Container;
import io.jenkins.blueocean.rest.model.Containers;
import org.jenkinsci.plugins.workflow.graph.FlowNode;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Pipeline Run
 *
 * @author Vivek Pandey
 */
public class PipelineRunImpl extends AbstractRunImpl<WorkflowRun> {
    public PipelineRunImpl(WorkflowRun run, Link parent) {
        super(run, parent);
    }

    @Override
    public Container<ChangeSetResource> getChangeSet() {
        Map<String, ChangeSetResource> m = new LinkedHashMap<>();
        int cnt = 0;
        for (ChangeLogSet<? extends Entry> cs : run.getChangeSets()) {
            for (ChangeLogSet.Entry e : cs) {
                cnt++;
                String id = e.getCommitId();
                if (id == null) id = String.valueOf(cnt);
                m.put(id, new ChangeSetResource(e));
            }
        }
        return Containers.fromResourceMap(getLink(),m);
    }

    @Override
    public BluePipelineNodeContainer getNodes() {
        if (run != null) {
            return new PipelineNodeContainerImpl(run, getLink());
        }
        return null;
    }

    @Override
    public Container<?> getSteps() {
        PipelineNodeGraphBuilder graphBuilder = new PipelineNodeGraphBuilder(run);
        List<FlowNode> nodes = graphBuilder.getAllSteps();
        List<BluePipelineStep> pipelineSteps = new ArrayList<>();
        for(FlowNode node:nodes){
            pipelineSteps.add(new PipelineStepImpl(node, graphBuilder, getLink().rel(BlueRun.STEPS)));
        }
        return Containers.fromResource(getLink().rel(BlueRun.STEPS), pipelineSteps);
    }

    @Override
    public BlueRunStopResponse stop() {
        run.doStop();
        return new BlueRunStopResponse(getStateObj(),getResult());
    }
}
