import { capabilityAugmenter, Fetch, UrlConfig, Utils, AppConfig } from '@jenkins-cd/blueocean-core-js';

import GithubApiUtils from './GithubApiUtils';

// TODO: temporary until we get more structured errors
const INVALID_TOKEN = 'Invalid accessToken';
const INVALID_SCOPES = 'missing scopes';
const INVALID_API_URL = 'Invalid apiUrl';


/**
 * Handles lookup, validation and creation the Github access token credential.
 */
export class GithubCredentialsApi {

    constructor(scmId) {
        this._fetch = Fetch.fetchJSON;
        this.organization = AppConfig.getOrganizationName();
        this.scmId = scmId || 'github';
    }

    findExistingCredential(apiUrl) {
        const path = UrlConfig.getJenkinsRootURL();
        let credUrl = Utils.cleanSlashes(`${path}/blue/rest/organizations/${this.organization}/scm/${this.scmId}`);
        credUrl = GithubApiUtils.appendApiUrlParam(credUrl, apiUrl);

        return this._fetch(credUrl)
            .then(credential => capabilityAugmenter.augmentCapabilities(credential));
    }

    createAccessToken(accessToken, apiUrl) {
        const path = UrlConfig.getJenkinsRootURL();
        let tokenUrl = Utils.cleanSlashes(`${path}/blue/rest/organizations/${this.organization}/scm/${this.scmId}/validate`);
        tokenUrl = GithubApiUtils.appendApiUrlParam(tokenUrl, apiUrl);

        const requestBody = {
            accessToken,
        };

        const fetchOptions = {
            method: 'PUT',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify(requestBody),
        };

        return this._fetch(tokenUrl, { fetchOptions })
            .then(data => capabilityAugmenter.augmentCapabilities(data))
            .then(
                token => this._createAccessTokenSuccess(token),
                error => this._createAccessTokenFailure(error)
            );
    }

    _createAccessTokenSuccess(credential) {
        return {
            success: true,
            credential,
        };
    }

    _createAccessTokenFailure(error) {
        const { code, message } = error.responseBody;
        const invalidApiUrl = code === 404 || message.indexOf(INVALID_API_URL) !== -1;

        return {
            success: false,
            invalid: message.indexOf(INVALID_TOKEN) !== -1,
            scopes: message.indexOf(INVALID_SCOPES) !== -1,
            invalidApiUrl,
        };
    }

}
