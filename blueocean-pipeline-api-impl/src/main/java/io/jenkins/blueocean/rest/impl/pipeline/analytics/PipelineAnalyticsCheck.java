package io.jenkins.blueocean.rest.impl.pipeline.analytics;

import hudson.Extension;
import hudson.model.Item;
import io.jenkins.blueocean.analytics.JobAnalyticsCheck;
import jenkins.branch.MultiBranchProject;
import org.kohsuke.accmod.Restricted;
import org.kohsuke.accmod.restrictions.NoExternalUse;

@Extension
@Restricted(NoExternalUse.class)
public class PipelineAnalyticsCheck extends JobAnalyticsCheck {

    @Override
    public String getName() {
        return "pipeline";
    }

    @Override
    public Boolean apply(Item item) {
        return item instanceof MultiBranchProject;
    }
}
