import { action, observable } from 'mobx';

import PromiseDelayUtils from '../../util/PromiseDelayUtils';
import BbCredentialsApi from './BbCredentialsApi';
import BbCredentialState from './BbCredentialsState';
import { LoadError, SaveError } from './BbCredentialsApi';


const MIN_DELAY = 500;
const { delayBoth } = PromiseDelayUtils;


/**
 * Manages retrieving, validating and saving Bitbucket credential
 * Also holds the state of the credential for use in BbCredentialStep.
 */
class BbCredentialsManager {

    @observable
    stateId = null;

    @observable
    pendingValidation = false;


    configure(scmId, apiUrl) {
        this._credentialsApi = new BbCredentialsApi(scmId);
        this.apiUrl = apiUrl;
    }

    constructor(credentialsApi) {
        this._credentialsApi = credentialsApi;
    }

    @action
    findExistingCredential() {
        this.stateId = BbCredentialState.PENDING_LOADING_CREDS;
        return this._credentialsApi.findExistingCredential(this.apiUrl)
            .then(...delayBoth(MIN_DELAY))
            .catch(error => this._findExistingCredentialFailure(error));
    }

    @action
    _findExistingCredentialFailure(error) {
        if (error.type === LoadError.TOKEN_NOT_FOUND) {
            this.stateId = BbCredentialState.NEW_REQUIRED;
        } else if (error.type === LoadError.TOKEN_INVALID) {
            this.stateId = BbCredentialState.INVALID_CREDENTIAL;
        } else if (error.type === LoadError.TOKEN_REVOKED) {
            this.stateId = BbCredentialState.REVOKED_CREDENTIAL;
        } else {
            this.stateId = BbCredentialState.UNEXPECTED_ERROR_CREDENTIAL;
        }
    }

    @action
    createCredential(userName, password) {
        this.pendingValidation = true;

        return this._credentialsApi.createBbCredential(this.apiUrl, userName, password)
            .then(...delayBoth(MIN_DELAY))
            .then(response => this._createCredentialSuccess(response))
            .catch(error => this._onCreateTokenFailure(error));
    }

    @action
    _createCredentialSuccess(credential) {
        this.pendingValidation = false;
        this.stateId = BbCredentialState.SAVE_SUCCESS;
        return credential;
    }

    @action
    _onCreateTokenFailure(error) {
        this.pendingValidation = false;

        if (error.type === SaveError.INVALID_CREDENTIAL) {
            this.stateId = BbCredentialState.INVALID_CREDENTIAL;
        } else {
            throw error;
        }
    }

}

export default BbCredentialsManager;
