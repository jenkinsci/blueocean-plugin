package io.jenkins.blueocean.rest.impl.pipeline.analytics;

import hudson.Extension;
import hudson.matrix.MatrixProject;
import hudson.model.Item;
import io.jenkins.blueocean.service.embedded.analytics.JobAnalyticsCheck;
import io.jenkins.blueocean.service.embedded.analytics.JobAnalyticsExclude;
import org.kohsuke.accmod.Restricted;
import org.kohsuke.accmod.restrictions.NoExternalUse;

@Extension
@Restricted(NoExternalUse.class)
public final class MatrixAnalyticsCheck implements JobAnalyticsCheck {
    @Override
    public String getName() {
        return "matrix";
    }

    @Override
    public Boolean apply(Item item) {
        return item instanceof MatrixProject;
    }

    /**
     * Exclude any jobs that are children of a MatrixProject
     */
    @Extension
    @Restricted(NoExternalUse.class)
    public final static class ExcludeImpl implements JobAnalyticsExclude {
        @Override
        public Boolean apply(Item item) {
            return item.getClass().getName().equals("hudson.matrix.MatrixConfiguration") || item.getParent() instanceof MatrixProject;
        }
    }
}
