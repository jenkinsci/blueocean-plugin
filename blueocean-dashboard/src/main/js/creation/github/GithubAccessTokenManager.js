import { action, observable } from 'mobx';

import waitAtLeast from '../flow2/waitAtLeast';
import { GithubAccessTokenState } from './GithubAccessTokenState';

const MIN_DELAY = 500;


/**
 * Manages retrieving, validating and saving the Github access token.
 * Also holds the state of the token for use in GithubCredentialStep.
 */
export class GithubAccessTokenManager {

    @observable
    stateId = null;

    @observable
    pendingValidation = false;

    credential = null;

    get credentialId() {
        return this.credential && this.credential.credentialId;
    }


    constructor(credentialsApi) {
        this._credentialsApi = credentialsApi;
    }

    findExistingCredential() {
        return this._credentialsApi.findExistingCredential()
            .then(waitAtLeast(MIN_DELAY))
            .then(
                cred => this._findExistingCredentialSuccess(cred),
                () => this._findExistingCredentialFailure(),
            );
    }

    _findExistingCredentialSuccess(credential) {
        if (credential && credential.credentialId) {
            this.credential = credential;
            return true;
        }

        return false;
    }

    @action
    _findExistingCredentialFailure() {
        this.stateId = GithubAccessTokenState.NEW_REQUIRED;
        return false;
    }

    @action
    markTokenRevoked() {
        this.credential = null;
        this.stateId = GithubAccessTokenState.EXISTING_REVOKED;
    }

    @action
    markTokenInvalidScopes() {
        this.credential = null;
        this.stateId = GithubAccessTokenState.EXISTING_MISSING_SCOPES;
    }

    @action
    createAccessToken(token, apiUrl) {
        this.pendingValidation = true;

        return this._credentialsApi.createAccessToken(token, apiUrl)
            .then(waitAtLeast(MIN_DELAY))
            .then(response => this._createTokenComplete(response));
    }

    @action
    _createTokenComplete(response) {
        this.pendingValidation = false;

        if (response.success) {
            this.credential = response.credential;
            this.stateId = GithubAccessTokenState.SAVE_SUCCESS;
        } else if (response.invalid) {
            this.stateId = GithubAccessTokenState.VALIDATION_FAILED_TOKEN;
        } else if (response.scopes) {
            this.stateId = GithubAccessTokenState.VALIDATION_FAILED_SCOPES;
        } else if (response.invalidApiUrl) {
            this.stateId = GithubAccessTokenState.VALIDATION_FAILED_API_URL;
        } else {
            this.stateId = GithubAccessTokenState.VALIDATION_FAILED_UNKNOWN;
        }

        return response;
    }

}
