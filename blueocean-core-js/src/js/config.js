/**
 * This config object comes from blueocean-config.
 */
const root = (typeof self === 'object' && self.self === self && self) ||
  (typeof global === 'object' && global.global === global && global) ||
  this;

const config = root.$blueoceanConfig || {};

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

    getInitialUser() {
        return this.getSecurityConfig().user;
    },

    getLoginUrl() {
        return this.getSecurityConfig().loginUrl;
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
