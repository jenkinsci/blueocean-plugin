package io.jenkins.blueocean.rest.impl.pipeline.analytics;

import hudson.Extension;
import hudson.model.Item;
import io.jenkins.blueocean.service.embedded.analytics.JobAnalyticsCheck;
import io.jenkins.blueocean.service.embedded.analytics.JobAnalyticsExclude;
import org.kohsuke.accmod.Restricted;
import org.kohsuke.accmod.restrictions.NoExternalUse;

@Extension
@Restricted(NoExternalUse.class)
public final class MavenAnalyticsCheck implements JobAnalyticsCheck {

    public static final String MAVEN_MODULE_SET_CLASS = "hudson.maven.MavenModuleSet";

    @Override
    public String getName() {
        return "maven";
    }

    @Override
    public Boolean apply(Item item) {
        return item.getClass().getName().equals(MAVEN_MODULE_SET_CLASS);
    }

    /**
     * Exclude any items that are MavenModule's or children of MavenModuleSet
     */
    @Extension
    @Restricted(NoExternalUse.class)
    public static final class ExcludeImpl implements JobAnalyticsExclude {
        @Override
        public Boolean apply(Item item) {
            return item.getClass().getName().equals("hudson.maven.MavenModule") || item.getParent().getClass().getName().equals(MAVEN_MODULE_SET_CLASS);
        }
    }
}
