package io.jenkins.blueocean.analytics;

import java.util.Map;

/**
 * Used when usage statistics is disabled or in development mode
 */
public final class NullAnalytics extends Analytics {

    static final NullAnalytics INSTANCE = new NullAnalytics();

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    protected void doTrack(String name, Map<String, Object> allProps) {
        // no-op
    }
}
