import { Fetch, UrlConfig, Utils, AppConfig } from '@jenkins-cd/blueocean-core-js';

import { TypedError } from '../TypedError';

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
const INVALID_API_URL = 'Invalid apiUrl';

/**
 * Handles lookup, validation and creation the Github access token credential.
 */
class PerforceCredentialsApi {
    constructor(scmId) {
        this._fetch = Fetch.fetchJSON;
        this.organization = AppConfig.getOrganizationName();
        this.scmId = scmId || 'perforce';
    }

    findExistingCredential(apiUrl) {
        const path = UrlConfig.getJenkinsRootURL();
        const credUrl = Utils.cleanSlashes(`${path}/blue/rest/organizations/${this.organization}/scm/${this.scmId}`);
        console.log("PerforceCredentialsApi: findExistingCredential: credUrl: " + credUrl);
        //return this._fetch("http://localhost:9090/jenkins/credentials/").then(result => this._findExistingCredentialSuccess(result), error => this._findExistingCredentialFailure(error));
        //return this._fetch("http://localhost:4567/user/getUsers").then(result => this._findExistingCredentialSuccess(result), error => this._findExistingCredentialFailure(error));
        return this._fetch("http://localhost:4567/user/getUsers");
        //http://localhost:4567/user/getUsers

    }

    _findExistingCredentialSuccess(credential) {
        console.log("credential.credentialId =" + credential.credentialId);
        //console.log("credential.loginName =" + credential.loginName);
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

    // createAccessToken(accessToken, apiUrl) {
    //     const path = UrlConfig.getJenkinsRootURL();
    //     let tokenUrl = Utils.cleanSlashes(`${path}/blue/rest/organizations/${this.organization}/scm/${this.scmId}/validate`);
    //     tokenUrl = GithubApiUtils.appendApiUrlParam(tokenUrl, apiUrl);
    //
    //     const requestBody = {
    //         accessToken,
    //     };
    //
    //     const fetchOptions = {
    //         method: 'PUT',
    //         headers: {
    //             'Content-Type': 'application/json',
    //         },
    //         body: JSON.stringify(requestBody),
    //     };
    //
    //     return this._fetch(tokenUrl, { fetchOptions }).catch(error => this._createAccessTokenFailure(error));
    // }
    //
    // _createAccessTokenFailure(error) {
    //     const { code, message } = error.responseBody;
    //
    //     if (code === 404 || message.indexOf(INVALID_API_URL) !== -1) {
    //         throw new TypedError(SaveError.API_URL_INVALID, error);
    //     } else if (message.indexOf(INVALID_TOKEN) !== -1) {
    //         throw new TypedError(SaveError.TOKEN_INVALID);
    //     } else if (message.indexOf(INVALID_SCOPES) !== -1) {
    //         throw new TypedError(SaveError.TOKEN_MISSING_SCOPES);
    //     }
    //
    //     throw error;
    // }
}

export default PerforceCredentialsApi;
