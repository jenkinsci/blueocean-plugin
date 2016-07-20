package io.jenkins.blueocean.service.embedded.rest;

import hudson.scm.ChangeLogSet;
import hudson.scm.ChangeLogSet.Entry;
import io.jenkins.blueocean.rest.hal.Link;
import io.jenkins.blueocean.rest.model.BlueChangeSetEntry;
import io.jenkins.blueocean.rest.model.BluePipelineNodeContainer;
import io.jenkins.blueocean.rest.model.BluePipelineStepContainer;
import io.jenkins.blueocean.rest.model.BlueRun;
import io.jenkins.blueocean.rest.model.Container;
import io.jenkins.blueocean.rest.model.Containers;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;

import java.util.LinkedHashMap;
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
    public Container<BlueChangeSetEntry> getChangeSet() {
        Map<String, BlueChangeSetEntry> m = new LinkedHashMap<>();
        int cnt = 0;
        for (ChangeLogSet<? extends Entry> cs : run.getChangeSets()) {
            for (ChangeLogSet.Entry e : cs) {
                cnt++;
                String id = e.getCommitId();
                if (id == null) id = String.valueOf(cnt);
                m.put(id, new ChangeSetResource(e, this));
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
    public BluePipelineStepContainer getSteps() {
        return new PipelineStepContainerImpl(null, new PipelineNodeGraphBuilder(run), getLink());
    }

    @Override
    public BlueRun stop() {
        run.doStop();
        return this;
    }
}
