package io.jenkins.blueocean.service.embedded.analytics;

import hudson.Extension;
import hudson.ProxyConfiguration;
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
@Extension(ordinal = -1)
@Restricted(NoExternalUse.class)
public class KeenAnalyticsImpl extends AbstractAnalytics {

    private static final KeenClient CLIENT = new JavaKeenClientBuilder().build();

    static {
        KeenClient.initialize(CLIENT);
    }

    public KeenAnalyticsImpl() {}

    @Override
    protected void doTrack(String name, Map<String, Object> allProps) {
        // Always set the proxy in case its configuration has changed after startup
        ProxyConfiguration proxyConfig = Jenkins.getInstance().proxy;
        Proxy proxy = proxyConfig == null ? null : proxyConfig.createProxy(null);
        CLIENT.setProxy(proxy);
        // Ensure that we are using the right project info
        KeenProject project = KeenConfiguration.get().project();
        CLIENT.setDefaultProject(project);
        // Send the event
        CLIENT.addEventAsync(name, allProps);
    }
}
