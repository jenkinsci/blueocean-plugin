package io.jenkins.blueocean.service.embedded.analytics;

import hudson.Extension;
import hudson.model.FreeStyleProject;
import hudson.model.Item;
import org.kohsuke.accmod.Restricted;
import org.kohsuke.accmod.restrictions.NoExternalUse;

@Extension
@Restricted(NoExternalUse.class)
public class FreeStyleAnalyticsCheck extends JobAnalyticsCheck {
    @Override
    public String getName() {
        return "freestyle";
    }

    @Override
    public Boolean apply(Item item) {
        return item instanceof FreeStyleProject;
    }
}
