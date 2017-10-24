package io.jenkins.blueocean.analytics;

import com.google.common.base.Objects;
import io.jenkins.blueocean.commons.BlueOceanConfigProperties;
import org.kohsuke.accmod.Restricted;

import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Used when usage statistics is disabled or in development mode
 */
public final class NullAnalytics extends Analytics {

    static final NullAnalytics INSTANCE = new NullAnalytics();

    private static final Logger LOGGER = Logger.getLogger(NullAnalytics.class.getName());

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    protected void doTrack(String name, Map<String, Object> allProps) {
        if (BlueOceanConfigProperties.isDevelopmentMode()) {
            LOGGER.log(Level.INFO, Objects.toStringHelper(this).add("name", name).add("props", allProps).toString());
        }
    }
}
