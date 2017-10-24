package io.jenkins.blueocean.service.embedded.analytics;

import com.google.common.base.Objects;
import hudson.Extension;
import hudson.model.UsageStatistics;
import io.jenkins.blueocean.commons.BlueOceanConfigProperties;
import org.kohsuke.accmod.Restricted;
import org.kohsuke.accmod.restrictions.NoExternalUse;

import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Used when usage statistics is disabled or in development mode
 */
@Extension
@Restricted(NoExternalUse.class)
public class NullAnalytics extends AbstractAnalytics {

    private static final Logger LOGGER = Logger.getLogger(NullAnalytics.class.getName());

    @Override
    public boolean isEnabled() {
        return BlueOceanConfigProperties.isDevelopmentMode() || UsageStatistics.DISABLED;
    }

    @Override
    protected void doTrack(String name, Map<String, Object> allProps) {
        if (BlueOceanConfigProperties.isDevelopmentMode()) {
            LOGGER.log(Level.INFO, Objects.toStringHelper(this).add("name", name).add("props", allProps).toString());
        }
    }
}
