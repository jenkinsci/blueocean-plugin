package io.jenkins.blueocean.service.embedded.analytics;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import hudson.ProxyConfiguration;
import io.jenkins.blueocean.commons.JsonConverter;
import jenkins.model.Jenkins;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.Proxy;
import java.net.URL;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class AnalyticsConfigService {

    private static final Logger LOGGER = Logger.getLogger(AnalyticsConfigService.class.getName());

    private static final String ANALYTICS_CONFIG_URL = "https://jenkins.io/projects/blueocean/analytics.json";

    public static AnalyticsConfig get() {
        return configSupplier.get();
    }

    private static Supplier<AnalyticsConfig> configSupplier = Suppliers.memoizeWithExpiration(() -> {
        AnalyticsConfig config;
        try {
            config = loadAnalyticsConfig();
        } catch (Throwable e) {
            config = AnalyticsConfig.EMPTY;
            LOGGER.log(Level.SEVERE, "Could not load analytics configuration", e);
        }
        return config;
    }, 1, TimeUnit.DAYS);

    private static AnalyticsConfig loadAnalyticsConfig() throws IOException {
        URL url = new URL("https://jenkins.io/projects/blueocean/analytics.json");
        ProxyConfiguration proxyConfig = Jenkins.getInstance().proxy;
        Proxy proxy = proxyConfig == null ? null : proxyConfig.createProxy(null);
        HttpURLConnection connection;
        if (proxy == null) {
            connection = (HttpURLConnection) url.openConnection();
        } else {
            connection = (HttpURLConnection) url.openConnection(proxy);
        }
        int resp = connection.getResponseCode();
        if (resp != 200) {
            throw new IOException("Received code " + resp + " when reading " + ANALYTICS_CONFIG_URL);
        }
        AnalyticsConfig config;
        try (InputStream is = connection.getInputStream()) {
            config = JsonConverter.toJava(is, AnalyticsConfig.class);
        }
        return config;
    }

    private AnalyticsConfigService() {
    }
}
