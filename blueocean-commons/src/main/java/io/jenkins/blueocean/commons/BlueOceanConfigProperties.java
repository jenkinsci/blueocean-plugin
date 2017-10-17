package io.jenkins.blueocean.commons;

/**
 * Common place put system properties that are used by blueocean modules.
 *
 * @author Ivan Meredith
 */
public class BlueOceanConfigProperties {
    public static final String BLUEOCEAN_FEATURE_JWT_AUTHENTICATION_PROPERTY = "BLUEOCEAN_FEATURE_JWT_AUTHENTICATION";
    public static final String BLUEOCEAN_ROLLBAR_ENABLED_PROPERTY = "BLUEOCEAN_ROLLBAR_ENABLED";

    public static final boolean ROLLBAR_ENABLED = Boolean.getBoolean(BLUEOCEAN_ROLLBAR_ENABLED_PROPERTY);
    public static final boolean BLUEOCEAN_FEATURE_JWT_AUTHENTICATION = Boolean.getBoolean(BLUEOCEAN_FEATURE_JWT_AUTHENTICATION_PROPERTY);

}
