package io.jenkins.blueocean.rest.impl.pipeline.analytics;

import hudson.Extension;
import hudson.model.FreeStyleProject;
import hudson.model.Item;
import io.jenkins.blueocean.service.embedded.analytics.JobAnalyticsCheck;
import org.kohsuke.accmod.Restricted;
import org.kohsuke.accmod.restrictions.NoExternalUse;

@Extension
@Restricted(NoExternalUse.class)
public class FreeStyleAnalyticsCheck implements JobAnalyticsCheck {
    @Override
    public String getName() {
        return "freestyle";
    }

    @Override
    public Boolean apply(Item item) {
        return item instanceof FreeStyleProject;
    }
}
