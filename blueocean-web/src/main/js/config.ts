/**
 * A simple holder for configuration information such as URLs etc
 *
 * Expose everything as getters, because some of these will change from plugin to plugin.
 */
export default class Config {
    private _appURLBase: string;
    private _rootURL: string;
    private _resourceURL: string;
    private _adjunctURL: string;
    private _serverBrowserTimeSkewMillis: string;

    constructor(options: any /* FIXME later */) {
        this._appURLBase = options.appURLBase || '';
        this._rootURL = options.rootURL || '';
        this._resourceURL = options.resourceURL || '';
        this._adjunctURL = options.adjunctURL || '';
        this._serverBrowserTimeSkewMillis = options.serverBrowserTimeSkewMillis || '';
    }

    getAppURLBase(): string {
        return this._appURLBase;
    }

    getRootURL(): string {
        return this._rootURL;
    }

    getResourceURL(): string {
        return this._resourceURL;
    }

    getAdjunctURL(): string {
        return this._adjunctURL;
    }

    getServerBrowserTimeSkewMillis() /*fix type */ {
        return this._serverBrowserTimeSkewMillis;
    }
}
