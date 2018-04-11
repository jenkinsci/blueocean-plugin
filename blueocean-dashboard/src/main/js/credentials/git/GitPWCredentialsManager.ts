import {action} from "mobx";

import {BbCredentialsState} from '../bitbucket/BbCredentialsState';
import {
    LoadError,
    SaveError,
} from '../bitbucket/BbCredentialsApi'; // TODO: move these out of BB tree?

import {GitPWCredentialsApi} from './GitPWCredentialsApi';

import PromiseDelayUtils from '../../util/PromiseDelayUtils';

const MIN_DELAY = 500;
const {delayBoth} = PromiseDelayUtils;

// TODO: Docs
// TODO: Unit tests
export class GitPWCredentialsManager {

    repositoryUrl?: string;
    branch: string = 'master';
    pendingValidation: boolean = false;
    stateId: BbCredentialsState;

    _api: GitPWCredentialsApi;

    constructor() {
    }

    // @action
    configure(repositoryUrl: string, branch?: string, api?: GitPWCredentialsApi) {
        this.repositoryUrl = repositoryUrl;

        if (typeof branch === 'string') {
            this.branch = branch;
        }

        this._api = api || new GitPWCredentialsApi();
    }

    @action
    findExistingCredential() {
        this.stateId = BbCredentialsState.PENDING_LOADING_CREDS;
        return this._api
            .findExistingCredential(this.repositoryUrl)
            .then(...delayBoth(MIN_DELAY))
            .catch(error => this._findExistingCredentialFailure(error));
    }

    @action
    _findExistingCredentialFailure(error) {
        // console.log('GitPWCredentialsManager._findExistingCredentialFailure', error.type, JSON.stringify(error, null, 4)); // TODO: RM
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
            .createCredential(this.repositoryUrl, userName, password)
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
