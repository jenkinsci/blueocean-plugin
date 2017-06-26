package io.jenkins.blueocean.commons.analytics;

import org.kohsuke.accmod.Restricted;
import org.kohsuke.accmod.restrictions.NoExternalUse;

import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

@Restricted(NoExternalUse.class)
public class NullAnalytics extends Analytics {

    private static final Logger LOGGER = Logger.getLogger(NullAnalytics.class.getName());

    static final Analytics INSTANCE = new NullAnalytics();

    @Override
    protected void doTrack(String name, Map<String, Object> allProps) {
        LOGGER.log(Level.FINE, "Analytics are disabled");
    }
}
