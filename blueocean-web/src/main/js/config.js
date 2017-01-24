/**
 * A simple holder for configuration information such as URLs etc
 *
 * Expose everything as getters, because some of these will change from plugin to plugin.
 */
export default class Config {

    constructor(options) {
        this._appURLBase = options.appURLBase || '';
        this._rootURL = options.rootURL || '';
        this._resourceURL = options.resourceURL || '';
        this._adjunctURL = options.adjunctURL || '';
        this._timeOffset = options.timeOffset || '';
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
}
