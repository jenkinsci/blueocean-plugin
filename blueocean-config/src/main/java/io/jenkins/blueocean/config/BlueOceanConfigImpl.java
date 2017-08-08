package io.jenkins.blueocean.config;

import java.util.HashMap;
import java.util.Map;

import hudson.Extension;
import io.jenkins.blueocean.rest.factory.BlueOceanConfigFactory;
import io.jenkins.blueocean.rest.model.BlueOceanConfig;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
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
        for (Map.Entry<Object, Object> entry : properties.entrySet()) {
            String ks = entry.getKey() == null ? "" : entry.getKey().toString();
            if (ks.startsWith(BlueOceanConfig.FEATURE_PROPERTY_PREFIX)) {
                Object value = entry.getValue();
                String vs = value == null ? "" : value.toString();
                if ("true".equalsIgnoreCase(vs) || "false".equalsIgnoreCase(vs)) {
                    value = Boolean.valueOf(vs);
                }
                config.put(ks.substring(BlueOceanConfig.FEATURE_PROPERTY_PREFIX.length()), value);
            }
        }
        for (Field f : BlueOceanConfig.class.getFields()) {
            if (Modifier.isStatic(f.getModifiers())
            && Modifier.isPublic(f.getModifiers())
            && f.getType() == String.class) {
                try {
                    String featureName = (String)f.get(null);
                    if (!BlueOceanConfig.FEATURE_PROPERTY_PREFIX.equals(featureName)
                    && !config.containsKey(featureName)) {
                        config.put(featureName, Boolean.FALSE);
                    }
                } catch (IllegalArgumentException | IllegalAccessException ex) {
                    throw new RuntimeException("Unable to read field: " + f.toString(), ex);
                }
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