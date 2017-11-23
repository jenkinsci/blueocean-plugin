package io.jenkins.blueocean.rest.impl.pipeline.analytics;

import hudson.Extension;
import hudson.matrix.MatrixProject;
import hudson.model.Item;
import io.jenkins.blueocean.service.embedded.analytics.JobAnalyticsCheck;
import org.kohsuke.accmod.Restricted;
import org.kohsuke.accmod.restrictions.NoExternalUse;

@Extension
@Restricted(NoExternalUse.class)
public class MatrixAnalyticsCheck extends JobAnalyticsCheck {
    @Override
    public String getName() {
        return "matrix";
    }

    @Override
    public Boolean apply(Item item) {
        return item instanceof MatrixProject;
    }
}
