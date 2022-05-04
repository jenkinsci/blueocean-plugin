import { Fetch, UrlConfig, Utils, AppConfig } from '@jenkins-cd/blueocean-core-js';
import { TypedError } from '../TypedError';

export const LoadError = {
    TOKEN_NOT_FOUND: 'TOKEN_NOT_FOUND',
    TOKEN_INVALID: 'TOKEN_INVALID',
    TOKEN_REVOKED: 'TOKEN_REVOKED',
};

export const SaveError = {
    INVALID_CREDENTIAL: 'INVALID_CREDENTIAL',
    UNKNOWN_ERROR: 'UNKNOWN_ERROR',
};

class BbCredentialsApi {
    constructor(scmId) {
        this._fetch = Fetch.fetchJSON;
        this.organization = AppConfig.getOrganizationName();
        this.scmId = scmId;
    }

    findExistingCredential(apiUrl) {
        const path = UrlConfig.getJenkinsRootURL();
        const credUrl = Utils.cleanSlashes(`${path}/blue/rest/organizations/${this.organization}/scm/${this.scmId}/?apiUrl=${apiUrl}`);

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

        if (responseBody.message.indexOf('Existing credential failed') >= 0) {
            throw new TypedError(LoadError.TOKEN_REVOKED, responseBody);
        }

        throw new TypedError(LoadError.TOKEN_INVALID, responseBody);
    }

    createBbCredential(apiUrl, userName, password) {
        const path = UrlConfig.getJenkinsRootURL();
        const validateCredUrl = Utils.cleanSlashes(`${path}/blue/rest/organizations/${this.organization}/scm/${this.scmId}/validate`);

        const requestBody = {
            userName,
            password,
            apiUrl,
        };

        const fetchOptions = {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify(requestBody),
        };

        return this._fetch(validateCredUrl, { fetchOptions }).catch(error => this._createAccessTokenFailure(error));
    }

    _createAccessTokenFailure(error) {
        const { code } = error.responseBody;

        if (code === 401) {
            throw new TypedError(SaveError.INVALID_CREDENTIAL, error);
        }

        throw new TypedError(SaveError.UNKNOWN_ERROR, error);
    }
}

export default BbCredentialsApi;
