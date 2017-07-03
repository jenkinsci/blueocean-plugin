package io.jenkins.blueocean.analytics;

import hudson.ExtensionPoint;
import io.jenkins.blueocean.analytics.Analytics.TrackRequest;

import java.util.Map;

/**
 * Enhance a request with properties when tracking an analytics event
 */
public abstract class AdditionalAnalyticsProperties implements ExtensionPoint {
    /**
     * Add additional properties for a {@link TrackRequest}
     * @param trackReq to be tracked
     * @return additional properties to be sent with the request or null
     */
    public abstract Map<String, Object> properties(TrackRequest trackReq);
}
