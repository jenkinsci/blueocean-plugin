package io.jenkins.blueocean.rest.impl.pipeline.analytics;

import hudson.Extension;
import hudson.model.Item;
import hudson.model.Job;
import io.jenkins.blueocean.service.embedded.analytics.JobAnalyticsCheck;
import io.jenkins.blueocean.rest.impl.pipeline.PrimaryBranch;
import jenkins.branch.MultiBranchProject;
import org.jenkinsci.plugins.pipeline.modeldefinition.actions.DeclarativeJobAction;
import org.kohsuke.accmod.Restricted;
import org.kohsuke.accmod.restrictions.NoExternalUse;

@Extension
@Restricted(NoExternalUse.class)
public class DeclarativePipelineAnalyticsCheck implements JobAnalyticsCheck {

    @Override
    public String getName() {
        return "pipelineDeclarative";
    }

    @Override
    public Boolean apply(Item item) {
        if (!(item instanceof MultiBranchProject)) {
            return false;
        }
        MultiBranchProject project = (MultiBranchProject)item;
        Job resolve = PrimaryBranch.resolve(project);
        return resolve != null && resolve.getAction(DeclarativeJobAction.class) != null;
    }
}
