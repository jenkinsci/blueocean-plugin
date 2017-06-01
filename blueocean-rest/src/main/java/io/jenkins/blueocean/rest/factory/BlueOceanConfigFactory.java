package io.jenkins.blueocean.rest.factory;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

import hudson.ExtensionList;
import hudson.ExtensionPoint;
import io.jenkins.blueocean.rest.model.BlueOceanConfig;

/**
 * Blue Ocean configuration factory which allows different extensions to return configuration values.
 * 
 */
public abstract class BlueOceanConfigFactory implements ExtensionPoint {

    /**
     * Returns Blue Ocean configuration
     * 
     * @return the current configuration
     */
    @Nonnull
    public abstract BlueOceanConfig getConfig();

    /**
     * Looks up for an specific config key on the available <code>BlueOceanConfigFactory</code> implementations
     * 
     * @param key the key to look for
     * @param type the type of the value
     * @return the value for the requested key
     */
    @CheckForNull
    public static <T> T getConfig(String key, Class<T> type) {
        for (BlueOceanConfigFactory configFactory : ExtensionList.lookup(BlueOceanConfigFactory.class)) {
            BlueOceanConfig config = configFactory.getConfig();
            T value = config.get(key, type);
            if (value != null) {
                return value;
            }
        }
        return null;
    }

    public static ExtensionList<BlueOceanConfigFactory> all() {
        return ExtensionList.lookup(BlueOceanConfigFactory.class);
    }
}
