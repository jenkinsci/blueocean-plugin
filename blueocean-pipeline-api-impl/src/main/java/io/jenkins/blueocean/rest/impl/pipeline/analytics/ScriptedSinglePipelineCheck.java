package io.jenkins.blueocean.rest.impl.pipeline.analytics;

import hudson.Extension;
import hudson.model.Item;
import io.jenkins.blueocean.service.embedded.analytics.JobAnalyticsCheck;
import jenkins.branch.MultiBranchProject;
import org.jenkinsci.plugins.pipeline.modeldefinition.actions.DeclarativeJobAction;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.kohsuke.accmod.Restricted;
import org.kohsuke.accmod.restrictions.NoExternalUse;

@Extension
@Restricted(NoExternalUse.class)
public class ScriptedSinglePipelineCheck implements JobAnalyticsCheck {
    @Override
    public String getName() {
        return "singlePipelineScripted";
    }

    @Override
    public Boolean apply(Item item) {
        if (item instanceof WorkflowJob) {
            WorkflowJob job = (WorkflowJob)item;
            return !(job.getParent() instanceof MultiBranchProject) && job.getAction(DeclarativeJobAction.class) == null;
        }
        return false;
    }
}
