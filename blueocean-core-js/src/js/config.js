/**
 * This config object comes from blueocean-config.
 */
import { blueocean } from './scopes';

const config = blueocean.config || {};
const features = config.features || {};

export default {
    getConfig() {
        return config;
    },

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

    isFeatureEnabled(name, defaultValue) {
        const value = features[name];
        if (typeof value === 'boolean') {
            return value;
        }
        if (typeof defaultValue === 'boolean') {
            return defaultValue;
        }
        return false;
    },

    showOrg() {
        return this.isFeatureEnabled('organizations.enabled', false);
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
