import {action} from "mobx";

import {BbCredentialsState} from '../bitbucket/BbCredentialsState';
import {
    LoadError,
    SaveError,
} from '../bitbucket/BbCredentialsApi'; // TODO: move these out of BB tree?

import {GitPWCredentialsApi} from './GitPWCredentialsApi';

import PromiseDelayUtils from '../../util/PromiseDelayUtils';
const MIN_DELAY = 500;
const { delayBoth } = PromiseDelayUtils;

// TODO: Docs
export class GitPWCredentialsManager {

    scmId: string;
    apiUrl?: string; // TODO: Rename to repo url
    pendingValidation: boolean = false;
    stateId: BbCredentialsState;

    _api: GitPWCredentialsApi;

    constructor() {
        // TODO: Allow construct with alt/mock API
        this._api = new GitPWCredentialsApi();
    }

    configure(scmId, apiUrl) {
        this.scmId = scmId;
        this.apiUrl = apiUrl;
    }

    @action
    findExistingCredential() {
        this.stateId = BbCredentialsState.PENDING_LOADING_CREDS;
        return this._api
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

        return this._api
            .createGitPWCredential(this.apiUrl, userName, password)
            .then(...delayBoth(MIN_DELAY))
            .then(response => this._createCredentialSuccess(response))
            .catch(error => this._onCreateCredentialFailure(error));
    }

    @action
    _createCredentialSuccess(credential) {
        this.pendingValidation = false;
        this.stateId = BbCredentialsState.SAVE_SUCCESS;
        return credential;
    }

    @action
    _onCreateCredentialFailure(error) {
        this.pendingValidation = false;

        if (error.type === SaveError.INVALID_CREDENTIAL) {
            this.stateId = BbCredentialsState.INVALID_CREDENTIAL;
        } else {
            throw error;
        }
    }
}
