import { Fetch, UrlConfig, Utils, AppConfig } from '@jenkins-cd/blueocean-core-js';

/**
 * Handles lookup and creation of Github servers.
 */
class GHEServerApi {
    constructor() {
        this._fetch = Fetch.fetchJSON;
        this.organization = AppConfig.getOrganizationName();
        this.scmId = 'github-enterprise';
    }

    listServers() {
        const path = UrlConfig.getJenkinsRootURL();
        const serversUrl = Utils.cleanSlashes(`${path}/blue/rest/organizations/${this.organization}/scm/${this.scmId}/servers`);

        return this._fetch(serversUrl);
    }

    createServer(serverName, serverUrl) {
        const path = UrlConfig.getJenkinsRootURL();
        const createUrl = Utils.cleanSlashes(`${path}/blue/rest/organizations/${this.organization}/scm/${this.scmId}/servers`);

        const requestBody = {
            name: serverName,
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

export default GHEServerApi;
