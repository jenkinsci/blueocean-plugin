package io.jenkins.blueocean.rest.impl.pipeline.analytics;

import hudson.Extension;
import hudson.matrix.MatrixConfiguration;
import hudson.model.AbstractProject;
import hudson.model.Item;
import io.jenkins.blueocean.service.embedded.analytics.JobAnalyticsCheck;
import org.kohsuke.accmod.Restricted;
import org.kohsuke.accmod.restrictions.NoExternalUse;

@Extension(ordinal = Integer.MAX_VALUE)
@Restricted(NoExternalUse.class)
public class OtherJobCheck extends JobAnalyticsCheck {
    @Override
    public String getName() {
        return "other";
    }

    @Override
    public Boolean apply(Item item) {
        return item instanceof AbstractProject // must be a project
            && !(item instanceof MatrixConfiguration)  // Individual matrix configurations
            && !item.getClass().getName().equals("hudson.maven.MavenModule"); // Ignore maven modules
    }
}
