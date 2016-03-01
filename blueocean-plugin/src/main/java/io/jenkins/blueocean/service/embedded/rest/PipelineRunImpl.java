package io.jenkins.blueocean.service.embedded.rest;

import hudson.scm.ChangeLogSet;
import hudson.scm.ChangeLogSet.Entry;
import io.jenkins.blueocean.rest.model.Container;
import io.jenkins.blueocean.rest.model.Containers;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;

import java.util.HashMap;
import java.util.Map;

/**
 * Pipeline Run
 *
 * @author Vivek Pandey
 */
public class PipelineRunImpl extends AbstractRunImpl<WorkflowRun> {
    public PipelineRunImpl(WorkflowRun run) {
        super(run);
    }

    @Override
    public String getBranch() {
        return run.getParent().getName();
    }

    @Override
    public String getCommitId() {
        return null;
    }

    @Override
    public Container<?> getChangeSet() {
        Map<String,Object> m = new HashMap<>();
        int cnt=0;
        for (ChangeLogSet<? extends Entry> cs : run.getChangeSets()) {
            for (ChangeLogSet.Entry e : cs) {
                cnt++;
                String id = e.getCommitId();
                if (id==null)   id = String.valueOf(cnt);
                m.put(id,e);
            }
        }
        return Containers.from(m);
    }
}
