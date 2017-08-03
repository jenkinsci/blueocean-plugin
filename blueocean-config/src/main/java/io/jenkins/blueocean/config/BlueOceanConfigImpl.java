package io.jenkins.blueocean.config;

import java.util.HashMap;
import java.util.Map;

import hudson.Extension;
import io.jenkins.blueocean.rest.factory.BlueOceanConfigFactory;
import io.jenkins.blueocean.rest.model.BlueOceanConfig;
import java.util.Properties;

/**
 * Default implementation for <code>BlueOceanConfig</code>
 *
 */
public class BlueOceanConfigImpl extends BlueOceanConfig {
    private static final Map<String, Object> config = new HashMap<>();
    private static final BlueOceanConfigImpl blueoceanConfig = new BlueOceanConfigImpl();

    private BlueOceanConfigImpl() {
        Properties properties = System.getProperties();
        for (Object key : properties.keySet()) {
            String ks = key == null ? "" : key.toString();
            if (ks.startsWith(BlueOceanConfig.FEATURE_PROPERTY_PREFIX)) {
                Object value = properties.get(key);
                String vs = value == null ? "" : value.toString();
                if ("true".equalsIgnoreCase(vs) || "false".equalsIgnoreCase(vs)) {
                    value = Boolean.valueOf(vs);
                }
                config.put(ks.substring(BlueOceanConfig.FEATURE_PROPERTY_PREFIX.length()), value);
            }
        }
    }

    @Override
    public Iterable<String> keys() {
        return config.keySet();
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