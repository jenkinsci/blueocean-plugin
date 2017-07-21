import { action, observable } from 'mobx';

import PromiseDelayUtils from '../../util/PromiseDelayUtils';

import GithubCredentialsApi from './GithubCredentialsApi';
import GithubAccessTokenState from './GithubAccessTokenState';
import { LoadError, SaveError } from './GithubCredentialsApi';

const MIN_DELAY = 500;
const { delayBoth } = PromiseDelayUtils;


/**
 * Manages retrieving, validating and saving the Github access token.
 * Also holds the state of the token for use in GithubCredentialStep.
 */
class GithubAccessTokenManager {

    @observable
    stateId = null;

    @observable
    pendingValidation = false;

    configure(scmId, apiUrl) {
        this._credentialsApi = new GithubCredentialsApi(scmId);
        this.apiUrl = apiUrl;
    }

    @action
    findExistingCredential() {
        this.stateId = GithubAccessTokenState.PENDING_LOADING_CREDS;
        return this._credentialsApi.findExistingCredential(this.apiUrl)
            .then(...delayBoth(MIN_DELAY))
            .catch(error => this._findExistingCredentialFailure(error));
    }

    @action
    _findExistingCredentialFailure(error) {
        if (error.type === LoadError.TOKEN_NOT_FOUND) {
            this.stateId = GithubAccessTokenState.NEW_REQUIRED;
        } else if (error.type === LoadError.TOKEN_REVOKED) {
            this.stateId = GithubAccessTokenState.EXISTING_REVOKED;
        } else if (error.type === LoadError.TOKEN_MISSING_SCOPES) {
            this.stateId = GithubAccessTokenState.EXISTING_MISSING_SCOPES;
        } else {
            this.stateId = GithubAccessTokenState.ERROR_UNKNOWN;
        }
    }

    @action
    createAccessToken(token) {
        this.pendingValidation = true;

        return this._credentialsApi.createAccessToken(token, this.apiUrl)
            .then(...delayBoth(MIN_DELAY))
            .then(cred => this._onCreateTokenSuccess(cred))
            .catch(error => this._onCreateTokenFailure(error));
    }

    @action
    _onCreateTokenSuccess(credential) {
        this.pendingValidation = false;
        this.stateId = GithubAccessTokenState.SAVE_SUCCESS;
        return credential;
    }

    @action
    _onCreateTokenFailure(error) {
        this.pendingValidation = false;

        if (error.type === SaveError.TOKEN_INVALID) {
            this.stateId = GithubAccessTokenState.VALIDATION_FAILED_TOKEN;
        } else if (error.type === SaveError.TOKEN_MISSING_SCOPES) {
            this.stateId = GithubAccessTokenState.VALIDATION_FAILED_SCOPES;
        } else {
            throw error;
        }
    }

}

export default GithubAccessTokenManager;
