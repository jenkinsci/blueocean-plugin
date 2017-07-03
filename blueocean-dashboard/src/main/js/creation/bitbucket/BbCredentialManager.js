import { action, observable } from 'mobx';

import waitAtLeast from '../flow2/waitAtLeast';
import { BbCredentialState } from './BbCredentialState';

const MIN_DELAY = 500;


/**
 * Manages retrieving, validating and saving BitBucket credential
 * Also holds the state of the credential for use in BbCredentialStep.
 */
export class BbCredentialManager {

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

    findExistingCredential(apiUrl) {
        return this._credentialsApi.findExistingCredential(apiUrl)
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
        this.stateId = BbCredentialState.NEW_REQUIRED;
        return false;
    }

    @action
    createCredential(apiUrl, userName, password) {
        this.pendingValidation = true;

        return this._credentialsApi.createBbCredential(apiUrl, userName, password)
            .then(waitAtLeast(MIN_DELAY))
            .then(response => this._createCredentialComplete(response));
    }

    @action
    _createCredentialComplete(response) {
        this.pendingValidation = false;

        if (response.success) {
            this.credential = response.credential;
            this.stateId = BbCredentialState.SAVE_SUCCESS;
        }

        return response;
    }

}
