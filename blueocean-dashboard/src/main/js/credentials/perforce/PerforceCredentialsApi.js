import {Fetch, UrlConfig, Utils, AppConfig} from '@jenkins-cd/blueocean-core-js';

import {TypedError} from '../TypedError';

export const LoadError = {
    CRED_NOT_FOUND: 'TOKEN_NOT_FOUND',
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

/**
 * Handles lookup, validation and creation the Github access token credential.
 */
class PerforceCredentialsApi {
    constructor(scmId) {
        this._fetch = Fetch.fetchJSON;
        this.organization = AppConfig.getOrganizationName();
        this.scmId = scmId || 'perforce';
    }

    findExistingCredential() {
        const path = UrlConfig.getJenkinsRootURL(); // Value is /jenkins
        const credUrl = Utils.cleanSlashes(`${path}/credentials/store/system/domain/_/api/json?tree=credentials[id,typeName]`);
        const fetchOptions = {
            method: 'GET',
            headers: {
                'Content-Type': 'application/json',
            },
        };
        return this._fetch(credUrl, {fetchOptions}).then(result => this._findExistingCredentialSuccess(result), error => this._findExistingCredentialFailure(error));
    }

    _findExistingCredentialSuccess(credential) {
        if (credential && credential.credentials) {

            return credential.credentials;
        }

        throw new TypedError(LoadError.TOKEN_NOT_FOUND);
    }

    _findExistingCredentialFailure(error) {
        const {responseBody} = error;
        const {message} = responseBody;

        if (message.indexOf(INVALID_TOKEN) !== -1) {
            throw new TypedError(LoadError.TOKEN_REVOKED, responseBody);
        } else if (message.indexOf(INVALID_SCOPES) !== -1) {
            throw new TypedError(LoadError.TOKEN_MISSING_SCOPES, responseBody);
        }

        throw error;
    }

}

export default PerforceCredentialsApi;
