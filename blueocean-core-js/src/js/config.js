/**
 * This config object comes from blueocean-config.
 */
const root = (typeof self === 'object' && self.self === self && self) ||
  (typeof global === 'object' && global.global === global && global) ||
  this;

const config = root.$blueoceanConfig;

export default {
    getJenkinsConfig() {
        return config.jenkinsConfig;
    },

    getSecurityConfig() {
        return config.jenkinsConfig.security;
    },

    isJWTEnabled() {
        return this.getSecurityConfig().enableJWT;
    },
};
