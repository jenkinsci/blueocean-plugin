package io.jenkins.blueocean.service.embedded.analytics;

import hudson.Extension;
import hudson.model.UsageStatistics;
import org.kohsuke.accmod.Restricted;
import org.kohsuke.accmod.restrictions.NoExternalUse;

import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Used when {@link UsageStatistics#DISABLED} is true
 */
@Extension
@Restricted(NoExternalUse.class)
public class NullAnalytics extends AbstractAnalytics {

    private static final Logger LOGGER = Logger.getLogger(NullAnalytics.class.getName());

    @Override
    public boolean isEnabled() {
        return UsageStatistics.DISABLED;
    }

    @Override
    protected void doTrack(String name, Map<String, Object> allProps) {
        LOGGER.log(Level.FINE, "Analytics are disabled");
    }
}
