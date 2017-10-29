package io.blueocean.ath;

import javax.inject.Singleton;
import java.io.File;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Really simple property lookup, resolved in order from:
 *
 * System.getProperty
 * environment
 * configFile
 * def value
 */
@Singleton
public class Config {
    private final Map<String, String> props = new HashMap<>();

    public Config() {
    }

    public void loadProps(File configFile) {
        try {
            Properties props = new Properties();
            props.load(new FileReader(configFile));
            props.stringPropertyNames().forEach(key -> this.props.put(key, props.getProperty(key)));
        } catch(Exception e) {
            throw new RuntimeException(e);
        }
    }

    public String getString(String name) {
        return getString(name, null);
    }

    public String getString(String name, String def) {
        if (props.containsKey(name)) {
            return props.get(name);
        }
        String envValue = System.getenv(name);
        if (envValue != null) {
            return envValue;
        }
        return System.getProperty(name, def);
    }

    public boolean getBoolean(String name) {
        return getBoolean(name, false);
    }

    public boolean getBoolean(String name, boolean def) {
        String prop = getString(name);
        if (prop != null) {
            return Boolean.parseBoolean(prop);
        }
        return def;
    }

    public int getInt(String name) {
        return getInt(name, 0);
    }

    public int getInt(String name, int def) {
        String prop = getString(name);
        if (prop != null) {
            return Integer.parseInt(prop);
        }
        return def;
    }
}
