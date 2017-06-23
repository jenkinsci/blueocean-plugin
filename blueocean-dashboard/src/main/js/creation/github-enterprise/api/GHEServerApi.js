import { capabilityAugmenter, Fetch, UrlConfig, Utils, AppConfig } from '@jenkins-cd/blueocean-core-js';

import { Enum } from '../../flow2/Enum';


const INVALID_ACCESS_TOKEN_CODE = 428;
const INVALID_ACCESS_TOKEN_MSG = 'Invalid Github accessToken';
const INVALID_SCOPES_MSG = 'Github accessToken does not have required scopes';

export const ListOrganizationsOutcome = new Enum({
    SUCCESS: 'success',
    INVALID_TOKEN_REVOKED: 'revoked_token',
    INVALID_TOKEN_SCOPES: 'invalid_token_scopes',
    ERROR: 'error',
});


/**
 * Handles lookup of Github orgs and repos, and saving of the Github org folder.
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

        return this._fetch(serversUrl)
            .then(orgs => capabilityAugmenter.augmentCapabilities(orgs))
            .then(orgs => orgs);
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

        return this._fetch(createUrl, { fetchOptions })
            .then(pipeline => capabilityAugmenter.augmentCapabilities(pipeline))
            .then(
                server => server,
                error => this._createServerFailure(error)
            );
    }

    _createServerFailure(error) {

    }

}

export default GHEServerApi;
