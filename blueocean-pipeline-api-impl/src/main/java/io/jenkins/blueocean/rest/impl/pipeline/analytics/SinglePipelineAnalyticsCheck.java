package io.jenkins.blueocean.rest.impl.pipeline.analytics;

import hudson.Extension;
import hudson.model.Item;
import io.jenkins.blueocean.analytics.JobAnalyticsCheck;
import jenkins.branch.MultiBranchProject;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.kohsuke.accmod.Restricted;
import org.kohsuke.accmod.restrictions.NoExternalUse;

@Extension
@Restricted(NoExternalUse.class)
public final class SinglePipelineAnalyticsCheck extends JobAnalyticsCheck {
    @Override
    public String getName() {
        return "singlePipeline";
    }

    @Override
    public Boolean apply(Item item) {
        return item instanceof WorkflowJob && !(item.getParent() instanceof MultiBranchProject);
    }
}
