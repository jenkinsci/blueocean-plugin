import { action, observable } from 'mobx';

import PromiseDelayUtils from '../../util/PromiseDelayUtils';
import BbCredentialsApi from './BbCredentialsApi';
import BbCredentialsState from './BbCredentialsState';
import { LoadError, SaveError } from './BbCredentialsApi';

const MIN_DELAY = 500;
const { delayBoth } = PromiseDelayUtils;

/**
 * Manages retrieving, validating and saving Bitbucket credential
 * Also holds the state of the credential for use in BbCredentialStep.
 */
class BbCredentialsManager {
    @observable stateId = null;

    @observable pendingValidation = false;

    configure(scmId, apiUrl) {
        this._credentialsApi = new BbCredentialsApi(scmId);
        this.apiUrl = apiUrl;
    }

    constructor(credentialsApi) {
        this._credentialsApi = credentialsApi;
    }

    @action
    findExistingCredential() {
        this.stateId = BbCredentialsState.PENDING_LOADING_CREDS;
        return this._credentialsApi
            .findExistingCredential(this.apiUrl)
            .then(...delayBoth(MIN_DELAY))
            .catch(error => this._findExistingCredentialFailure(error));
    }

    @action
    _findExistingCredentialFailure(error) {
        if (error.type === LoadError.TOKEN_NOT_FOUND) {
            this.stateId = BbCredentialsState.NEW_REQUIRED;
        } else if (error.type === LoadError.TOKEN_INVALID) {
            this.stateId = BbCredentialsState.INVALID_CREDENTIAL;
        } else if (error.type === LoadError.TOKEN_REVOKED) {
            this.stateId = BbCredentialsState.REVOKED_CREDENTIAL;
        } else {
            this.stateId = BbCredentialsState.UNEXPECTED_ERROR_CREDENTIAL;
        }
    }

    @action
    createCredential(userName, password) {
        this.pendingValidation = true;

        return this._credentialsApi
            .createBbCredential(this.apiUrl, userName, password)
            .then(...delayBoth(MIN_DELAY))
            .then(response => this._createCredentialSuccess(response))
            .catch(error => this._onCreateTokenFailure(error));
    }

    @action
    _createCredentialSuccess(credential) {
        this.pendingValidation = false;
        this.stateId = BbCredentialsState.SAVE_SUCCESS;
        return credential;
    }

    @action
    _onCreateTokenFailure(error) {
        this.pendingValidation = false;

        if (error.type === SaveError.INVALID_CREDENTIAL) {
            this.stateId = BbCredentialsState.INVALID_CREDENTIAL;
        } else {
            throw error;
        }
    }
}

export default BbCredentialsManager;
