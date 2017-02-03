/**
 * This config object comes from blueocean-config.
 */
import { blueocean } from './scopes';

const config = blueocean.config || {};
const env = config.env || {};

export default {
    getJenkinsConfig() {
        return config.jenkinsConfig || {};
    },

    getSecurityConfig() {
        return this.getJenkinsConfig().security || {};
    },

    isJWTEnabled() {
        return !!this.getSecurityConfig().enableJWT;
    },

    getLoginUrl() {
        return this.getSecurityConfig().loginUrl;
    },

    getPluginInfo(pluginId) {
        return blueocean.jsExtensions.find((pluginInfo) => pluginInfo.hpiPluginId === pluginId);
    },

    getEnvProperty(name, defaultValue) {
        const value = env[name];
        if (value !== undefined) {
            return value;
        }
        return defaultValue;
    },

    /**
     * Set a new "jenkinsConfig" object.
     * Useful for testing in a headless environment.
     * @param newConfig
     * @private
     */
    _setJenkinsConfig(newConfig) {
        config.jenkinsConfig = newConfig;
    },
};
