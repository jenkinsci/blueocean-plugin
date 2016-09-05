import UrlConfig from './urlconfig'
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

    getLoginUrl() {
        let loginUrl = this.getSecurityConfig().loginUrl;
        if (!loginUrl) {
            throw new Error('There is no login url. You should not need to login.');
        }
        if (!loginUrl.startsWith('/')) {
            loginUrl = `/${loginUrl}`;
        }
        return UrlConfig.getJenkinsRootURL() + loginUrl;
    },

};
