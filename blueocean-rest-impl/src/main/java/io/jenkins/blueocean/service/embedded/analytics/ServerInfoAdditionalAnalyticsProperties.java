package io.jenkins.blueocean.service.embedded.analytics;

import com.google.common.collect.Maps;
import hudson.Extension;
import hudson.Plugin;
import hudson.util.VersionNumber;
import io.jenkins.blueocean.analytics.AdditionalAnalyticsProperties;
import io.jenkins.blueocean.analytics.Analytics.TrackRequest;
import jenkins.model.Jenkins;

import java.util.Map;

/**
 * Add Jenkins and Blue Ocean versions to the properties tracked
 */
@Extension
public class ServerInfoAdditionalAnalyticsProperties extends AdditionalAnalyticsProperties {
    @Override
    public Map<String, Object> properties(TrackRequest trackReq) {
        Map<String, Object> props = Maps.newHashMap();
        VersionNumber version = Jenkins.getVersion();
        if (version != null && version.toString() != null) {
            props.put("jenkinsVersion", version.toString());
        }
        Plugin plugin = Jenkins.getInstance().getPlugin("blueocean-rest");
        if(plugin != null) {
            props.put("blueoceanVersion", plugin.getWrapper().getVersion());
        }

        return props;
    }
}
