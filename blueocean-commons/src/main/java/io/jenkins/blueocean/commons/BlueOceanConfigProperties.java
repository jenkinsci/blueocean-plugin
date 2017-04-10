package io.jenkins.blueocean.commons;

/**
 * Common place put system properties that are used by blueocean modules.
 *
 * @author Ivan Meredith
 */
public class BlueOceanConfigProperties {
    public static final boolean ROLLBAR_ENABLED = Boolean.getBoolean("BLUEOCEAN_ROLLBAR_ENABLED");

    public static final boolean BLUEOCEAN_FEATURE_JWT_AUTHENTICATION = Boolean.getBoolean("BLUEOCEAN_FEATURE_JWT_AUTHENTICATION");

}
