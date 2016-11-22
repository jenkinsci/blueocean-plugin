package io.jenkins.blueocean.indexing;

import hudson.Extension;
import hudson.model.Item;
import hudson.model.Job;
import hudson.model.Run;
import jenkins.branch.MultiBranchProject;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;

@Extension
public class MultiBranchProjectIndexStrategy extends IndexStrategy {
    @Override
    public Item findIndexParent(Run<?, ?> run) {
        Job<?, ?> parent = run.getParent();
        if (parent instanceof WorkflowJob && parent.getParent() instanceof MultiBranchProject) {
            return (Item) parent.getParent();
        }
        return null;
    }
}
