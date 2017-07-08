import { Fetch, UrlConfig, Utils, AppConfig } from '@jenkins-cd/blueocean-core-js';


/**
 * Handles lookup and creation of Github servers.
 */
class BbServerApi {

    constructor() {
        this._fetch = Fetch.fetchJSON;
        this.organization = AppConfig.getOrganizationName();
        this.scmId = 'bitbucket-server';
    }

    listServers() {
        const path = UrlConfig.getJenkinsRootURL();
        const serversUrl = Utils.cleanSlashes(`${path}/blue/rest/organizations/${this.organization}/scm/${this.scmId}/servers`);

        return this._fetch(serversUrl);
    }

    createServer(serverUrl) {
        const path = UrlConfig.getJenkinsRootURL();
        const createUrl = Utils.cleanSlashes(`${path}/blue/rest/organizations/${this.organization}/scm/${this.scmId}/servers`);

        const requestBody = {
            apiUrl: serverUrl,
        };

        const fetchOptions = {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify(requestBody),
        };

        return this._fetch(createUrl, { fetchOptions });
    }

}

export default BbServerApi;
