package io.jenkins.blueocean.rest.model;

import org.kohsuke.stapler.export.ExportedBean;

/**
 * Holds the Blue Ocean configuration
 *
 */
@ExportedBean
public abstract class BlueOceanConfig {
    public static final String FEATURE_PROPERTY_PREFIX = "blueocean.features.";
    public static final String ORGANIZATION_ENABLED = "organizations.enabled";
    public abstract <T> T get(String key, Class<T> type);
}
