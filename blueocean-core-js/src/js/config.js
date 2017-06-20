/**
 * This config object comes from blueocean-config.
 */
import { blueocean } from './scopes';

const config = blueocean.config || {};
const features = config.features || {};
const organization = blueocean.organization || {};


export default {
    loadUrls() {
        try {
            const headElement = document.getElementsByTagName('head')[0];

            // Look up where the Blue Ocean app is hosted
            config.blueoceanAppURL = headElement.getAttribute('data-appurl');

            // load server skewTime
            config.serverBrowserTimeSkewMillis = headElement.getAttribute('data-servertime') - Date.now();
            if (typeof config.blueoceanAppURL !== 'string') {
                config.blueoceanAppURL = '/';
            }

            config.jenkinsRootURL = headElement.getAttribute('data-rooturl');
            config.resourceUrl = headElement.getAttribute('data-resurl');

            config.isLoaded = true;
        } catch (e) {
            // headless escape
            config.jenkinsRootURL = '/jenkins';
            config.serverBrowserTimeSkewMillis = 0;
        }
    },

    getConfig() {
        return config;
    },

    getJenkinsConfig() {
        return config.jenkinsConfig || {};
    },

    getOrganizationName(encoded = true) {
        return encoded ? encodeURIComponent(organization.name) : organization.name;
    },
    
    getOrganizationDisplayName() {
        return organization.displayName;
    },

    getOrganizationGroup() {
        return organization.organizationGroup;
    },

    getSecurityConfig() {
        return this.getJenkinsConfig().security || {};
    },

    isJWTEnabled() {
        return !!this.getSecurityConfig().enableJWT;
    },

    getJWTServiceHostUrl() {
        return this.getSecurityConfig().jwtServiceHostUrl;
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

    getJenkinsRootURL() {
        if (!config.isLoaded) {
            this.loadUrls();
        }
        return (typeof config.jenkinsRootURL === 'string' ? config.jenkinsRootURL : '/jenkins');
    },

    getResourceURL() {
        if (!config.isLoaded) {
            this.loadUrls();
        }
        return (typeof config.resourceUrl === 'string' ? config.resourceUrl : '/jenkins/static');
    },

    getServerBrowserTimeSkewMillis() {
        if (!config.isLoaded) {
            this.loadUrls();
        }
        return (typeof config.serverBrowserTimeSkewMillis === 'number' ? config.serverBrowserTimeSkewMillis : 0);
    },


    getRestRoot() {
        return `${this.getJenkinsRootURL()}/blue/rest`;
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

