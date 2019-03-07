import { action, observable } from 'mobx';

import PromiseDelayUtils from '../../util/PromiseDelayUtils';

import PerforceCredentialsApi from './PerforceCredentialsApi';
import PerforceCredentialsState from './PerforceCredentialsState';
import { LoadError, SaveError } from './PerforceCredentialsApi';


const MIN_DELAY = 500;
const { delayBoth } = PromiseDelayUtils;

/**
 * Manages retrieving, validating and saving the Github access token.
 * Also holds the state of the token for use in GithubCredentialStep.
 */
class PerforceCredentialsManager {
    @observable stateId = null;

    @observable pendingValidation = false;

    configure(scmId, apiUrl) {
        this._credentialsApi = new PerforceCredentialsApi(scmId);
        this.apiUrl = apiUrl;
    }

    @action
    findExistingCredential() {
        this.stateId = PerforceCredentialsState.PENDING_LOADING_CREDS;
        return this._credentialsApi
            .findExistingCredential(this.apiUrl)
            .then(...delayBoth(MIN_DELAY))
            .catch(error => this._findExistingCredentialFailure(error));
    }

    @action
    _findExistingCredentialFailure(error) {
        if (error.type === LoadError.TOKEN_NOT_FOUND) {
            this.stateId = PerforceCredentialsState.NEW_REQUIRED;
        } else if (error.type === LoadError.TOKEN_REVOKED) {
            this.stateId = PerforceCredentialsState.EXISTING_REVOKED;
        } else if (error.type === LoadError.TOKEN_MISSING_SCOPES) {
            this.stateId = PerforceCredentialsState.EXISTING_MISSING_SCOPES;
        } else {
            this.stateId = PerforceCredentialsState.ERROR_UNKNOWN;
        }
    }

    /*@action
    createAccessToken(token) {
        this.pendingValidation = true;

        return this._credentialsApi
            .createAccessToken(token, this.apiUrl)
            .then(...delayBoth(MIN_DELAY))
            .then(cred => this._onCreateTokenSuccess(cred))
            .catch(error => this._onCreateTokenFailure(error));
    }

    @action
    _onCreateTokenSuccess(credential) {
        this.pendingValidation = false;
        this.stateId = GithubCredentialsState.SAVE_SUCCESS;
        return credential;
    }

    @action
    _onCreateTokenFailure(error) {
        this.pendingValidation = false;

        if (error.type === SaveError.TOKEN_INVALID) {
            this.stateId = GithubCredentialsState.VALIDATION_FAILED_TOKEN;
        } else if (error.type === SaveError.TOKEN_MISSING_SCOPES) {
            this.stateId = GithubCredentialsState.VALIDATION_FAILED_SCOPES;
        } else {
            throw error;
        }
    }*/
}

export default PerforceCredentialsManager;
