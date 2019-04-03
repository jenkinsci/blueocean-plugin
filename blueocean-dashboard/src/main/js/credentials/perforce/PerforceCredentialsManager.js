import {action, observable} from 'mobx';

import PromiseDelayUtils from '../../util/PromiseDelayUtils';

const MIN_DELAY = 500;
const {delayBoth} = PromiseDelayUtils;

/**
 * Manages retrieving, validating and saving the Perforce credentials.
 * Also holds the state of the credential for use in PerforceCredentialStep.
 */
class PerforceCredentialsManager {
    @observable credentials = [];


    constructor(credentialsApi) {
        this.credentialsApi = credentialsApi;
    }

    @action
    findExistingCredential() {
        return this.credentialsApi.findExistingCredential().then(credentials => this._onfindCredSuccess(credentials));
    }

    @action
    _onfindCredSuccess(credentials) {
        //this.credentials.replace(credentials);
        this.credentials = credentials.credentials;
        return credentials;
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
