package io.jenkins.blueocean.config;

import java.util.HashMap;
import java.util.Map;

import hudson.Extension;
import jenkins.util.SystemProperties;

/**
 * Default implementation for <code>BlueOceanConfig</code>
 *
 */
public class BlueOceanConfigImpl extends BlueOceanConfig {
    private static final Map<String, Object> config = new HashMap<>();
    private static final BlueOceanConfigImpl blueoceanConfig = new BlueOceanConfigImpl();

    private BlueOceanConfigImpl() {
        config.put(ORGANIZATION_ENABLED, SystemProperties.getBoolean(FEATURE_PROPERTY_PREFIX + ORGANIZATION_ENABLED));
    }

    @Override
    public <T> T get(String key, Class<T> type) {
        return (T) config.get(key);
    }

    @Extension(ordinal = -9999)
    public static class BlueOceanConfigFactoryImpl extends BlueOceanConfigFactory {
        public BlueOceanConfig getConfig() {
            return blueoceanConfig;
        }
    }
}