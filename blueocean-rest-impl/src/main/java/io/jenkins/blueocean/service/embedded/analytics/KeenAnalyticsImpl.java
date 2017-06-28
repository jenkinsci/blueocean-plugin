package io.jenkins.blueocean.service.embedded.analytics;

import hudson.Extension;
import hudson.ProxyConfiguration;
import hudson.model.UsageStatistics;
import io.keen.client.java.JavaKeenClientBuilder;
import io.keen.client.java.KeenClient;
import io.keen.client.java.KeenProject;
import jenkins.model.Jenkins;
import org.kohsuke.accmod.Restricted;
import org.kohsuke.accmod.restrictions.NoExternalUse;

import java.net.Proxy;
import java.util.Map;

/**
 * Tracks events using Keen.io
 */
@Extension
@Restricted(NoExternalUse.class)
public class KeenAnalyticsImpl extends AbstractAnalytics {

    private static final String PROJECT_ID = "5950934f3d5e150f5ab9d7be";
    private static final String WRITE_KEY = "E6C1FA3407AF4DD3115DBC186E40E9183A90069B1D8BBA78DB3EA6B15EA6182C881E8C55B4D7A48F55D5610AD46F36E65093227A7490BF7A56307047903BCCB16D05B9456F18A66849048F100571FDC91888CAD94F2A271A8B9E5342D2B9404E";
    private static final KeenClient CLIENT = new JavaKeenClientBuilder().build();

    static {
        KeenClient.initialize(CLIENT);
        KeenClient.client().setDefaultProject(new KeenProject(PROJECT_ID, WRITE_KEY, null));
    }

    public KeenAnalyticsImpl() {
    }

    @Override
    public boolean isEnabled() {
        return !UsageStatistics.DISABLED;
    }

    @Override
    protected void doTrack(String name, Map<String, Object> allProps) {
        // Always set the proxy in case its configuration has changed before startup
        ProxyConfiguration proxyConfig = Jenkins.getInstance().proxy;
        Proxy proxy = proxyConfig == null ? null : proxyConfig.createProxy(null);
        CLIENT.setProxy(proxy);
        CLIENT.addEventAsync(name, allProps);
    }
}
