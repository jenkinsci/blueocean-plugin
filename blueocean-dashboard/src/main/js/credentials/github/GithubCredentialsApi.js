import { Fetch, UrlConfig, Utils, AppConfig } from '@jenkins-cd/blueocean-core-js';

import GithubApiUtils from '../../creation/github/api/GithubApiUtils';
import { TypedError } from '../TypedError';

export const LoadError = {
    TOKEN_NOT_FOUND: 'TOKEN_NOT_FOUND',
    TOKEN_REVOKED: 'TOKEN_REVOKED',
    TOKEN_MISSING_SCOPES: 'TOKEN_MISSING_SCOPES',
};

export const SaveError = {
    TOKEN_INVALID: 'TOKEN_INVALID',
    TOKEN_MISSING_SCOPES: 'TOKEN_MISSING_SCOPES',
    API_URL_INVALID: 'API_URL_INVALID',
};

// TODO: temporary until we get more structured errors
const INVALID_TOKEN = 'Invalid accessToken';
const INVALID_SCOPES = 'missing scopes';
const INVALID_API_URL = 'Invalid apiUrl';

/**
 * Handles lookup, validation and creation the Github access token credential.
 */
class GithubCredentialsApi {
    constructor(scmId) {
        this._fetch = Fetch.fetchJSON;
        this.organization = AppConfig.getOrganizationName();
        this.scmId = scmId || 'github';
    }

    findExistingCredential(apiUrl) {
        const path = UrlConfig.getJenkinsRootURL();
        let credUrl = Utils.cleanSlashes(`${path}/blue/rest/organizations/${this.organization}/scm/${this.scmId}/`);
        credUrl = GithubApiUtils.appendApiUrlParam(credUrl, apiUrl);

        const fetchOptions = {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            }
        };

        return this._fetch(credUrl, {fetchOptions})
            .then(result => this._findExistingCredentialSuccess(result), error => this._findExistingCredentialFailure(error));
    }

    _findExistingCredentialSuccess(credential) {
        if (credential && credential.credentialId) {
            return credential;
        }

        throw new TypedError(LoadError.TOKEN_NOT_FOUND);
    }

    _findExistingCredentialFailure(error) {
        const { responseBody } = error;
        const { message } = responseBody;

        if (message.indexOf(INVALID_TOKEN) !== -1) {
            throw new TypedError(LoadError.TOKEN_REVOKED, responseBody);
        } else if (message.indexOf(INVALID_SCOPES) !== -1) {
            throw new TypedError(LoadError.TOKEN_MISSING_SCOPES, responseBody);
        }

        throw error;
    }

    createAccessToken(accessToken, apiUrl) {
        const path = UrlConfig.getJenkinsRootURL();
        let tokenUrl = Utils.cleanSlashes(`${path}/blue/rest/organizations/${this.organization}/scm/${this.scmId}/validate`);
        tokenUrl = GithubApiUtils.appendApiUrlParam(tokenUrl, apiUrl);

        const requestBody = {
            accessToken,
        };

        const fetchOptions = {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify(requestBody),
        };

        return this._fetch(tokenUrl, { fetchOptions }).catch(error => this._createAccessTokenFailure(error));
    }

    _createAccessTokenFailure(error) {
        const { code, message } = error.responseBody;

        if (code === 404 || message.indexOf(INVALID_API_URL) !== -1) {
            throw new TypedError(SaveError.API_URL_INVALID, error);
        } else if (message.indexOf(INVALID_TOKEN) !== -1) {
            throw new TypedError(SaveError.TOKEN_INVALID);
        } else if (message.indexOf(INVALID_SCOPES) !== -1) {
            throw new TypedError(SaveError.TOKEN_MISSING_SCOPES);
        }

        throw error;
    }
}

export default GithubCredentialsApi;
