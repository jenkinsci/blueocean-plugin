/**
 * A simple holder for configuration information such as URLs etc
 *
 * Expose everything as getters, because some of these will change from plugin to plugin.
 */
class Config {

    constructor(options) {
        this._appURLBase = options.appURLBase || '';
        this._rootURL = options.rootURL || '';
        this._resourceURL = options.resourceURL || '';
        this._adjunctURL = options.adjunctURL || '';
        this._serverBrowserTimeSkewMillis = options.serverBrowserTimeSkewMillis || '';
    }

    getAppURLBase() {
        return this._appURLBase;
    }

    getRootURL() {
        return this._rootURL;
    }

    getResourceURL() {
        return this._resourceURL;
    }

    getAdjunctURL() {
        return this._adjunctURL;
    }

    getServerBrowserTimeSkewMillis() {
        return this._serverBrowserTimeSkewMillis;
    }
}

/**
 * This config object comes from blueocean-config.
 */
import { blueocean } from './scopes';

const config = blueocean.config || {};
const features = config.features || {};
const organization = blueocean.organization || {};

// any all features added by ?features=SOMETHING,SOMETHING_ELSE
const pfx = 'features=';
const pfxlen = pfx.length;
const condition = window && window.location && window.location.href && window.location.href.split instanceof Function &&
    window.location.href.split('?').length > 0;
const query = condition ? window.location.href.split('?')[1] : undefined;
if (query) {
    query.split('&')
        .forEach(p => p.startsWith(pfx)
    && (p.substring(pfxlen).split(',').forEach(f => {
        features[f] = true;
})));
}
const corjsver = {
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

    getAnalyticsEnabled() {
        return this.getJenkinsConfig().analytics || false;
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

export default Object.assign(Config, corjsver);


//
// General "system" config information.
//
// TODO: This should be in a general sharable component.
// Passing it around in the react context is silly.
//

exports.loadConfig = function () {
    const headElement = document.getElementsByTagName('head')[0];

    // Look up where the Blue Ocean app is hosted
    exports.blueoceanAppURL = headElement.getAttribute('data-appurl');

    if (typeof exports.blueoceanAppURL !== 'string') {
        exports.blueoceanAppURL = '/';
    }

    exports.jenkinsRootURL = headElement.getAttribute('data-rooturl');
};

exports.getJenkinsRootURL = function getJenkinsRootURL() {
    if (!exports.jenkinsRootURL) {
        exports.loadConfig();
    }
    return exports.jenkinsRootURL;
};

exports.getRestRoot = function getRestRoot() {
    return `${exports.getJenkinsRootURL()}/blue/rest`;
};
