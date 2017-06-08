import { capabilityAugmenter, Fetch, UrlConfig, Utils, AppConfig } from '@jenkins-cd/blueocean-core-js';

// TODO: temporary until we get more structured errors
const INVALID_TOKEN = 'Invalid accessToken';
const INVALID_SCOPES = 'missing scopes';


/**
 * Handles lookup, validation and creation the Github access token credential.
 */
export class GithubCredentialsApi {

    constructor(scmId) {
        this._fetch = Fetch.fetchJSON;
        this.organization = AppConfig.getOrganizationName();
        this.scmId = scmId || 'github';
    }

    findExistingCredential() {
        const path = UrlConfig.getJenkinsRootURL();
        const credUrl = Utils.cleanSlashes(`${path}/blue/rest/organizations/${this.organization}/scm/${this.scmId}`);

        return this._fetch(credUrl)
            .then(credential => capabilityAugmenter.augmentCapabilities(credential));
    }

    createAccessToken(accessToken) {
        const path = UrlConfig.getJenkinsRootURL();
        const tokenUrl = Utils.cleanSlashes(`${path}/blue/rest/organizations/${this.organization}/scm/${this.scmId}/validate`);

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
        const { message } = error.responseBody;

        return {
            success: false,
            invalid: message.indexOf(INVALID_TOKEN) !== -1,
            scopes: message.indexOf(INVALID_SCOPES) !== -1,
        };
    }

}
