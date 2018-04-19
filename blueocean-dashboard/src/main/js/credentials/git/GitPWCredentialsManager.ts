import {action} from "mobx";

import {
    LoadError,
    SaveError,
} from '../bitbucket/BbCredentialsApi'; // TODO: move these out of BB tree?

import {GitPWCredentialsApi} from './GitPWCredentialsApi';

import PromiseDelayUtils from '../../util/PromiseDelayUtils';

const MIN_DELAY = 500;
const {delayBoth} = PromiseDelayUtils;

export enum ManagerState {
    PENDING_LOADING_CREDS,
    NEW_REQUIRED,
    SAVE_SUCCESS,
    INVALID_CREDENTIAL,
    REVOKED_CREDENTIAL,
    UNEXPECTED_ERROR_CREDENTIAL,
    PENDING_VALIDATION,
}

// TODO: Docs
// TODO: Unit tests
export class GitPWCredentialsManager {

    repositoryUrl?: string;
    branch: string = 'master';
    pendingValidation: boolean = false; // TODO: replace with enum
    // stateId: BbCredentialsState;  // TODO: replace with enum
    state: ManagerState;

    _api: GitPWCredentialsApi;

    constructor() {
    }

    configure(repositoryUrl: string, branch?: string, api?: GitPWCredentialsApi) {
        this.repositoryUrl = repositoryUrl;

        if (typeof branch === 'string') {
            this.branch = branch;
        }

        this._api = api || new GitPWCredentialsApi();
    }

    @action
    findExistingCredential() {
        this.state = ManagerState.PENDING_LOADING_CREDS;
        return this._api
            .findExistingCredential(this.repositoryUrl)
            .then(...delayBoth(MIN_DELAY))
            .catch(error => this._findExistingCredentialFailure(error));
    }

    @action
    _findExistingCredentialFailure(error) {
        if (error.type === LoadError.TOKEN_NOT_FOUND) {
            this.state = ManagerState.NEW_REQUIRED;
        } else if (error.type === LoadError.TOKEN_INVALID) {
            this.state = ManagerState.INVALID_CREDENTIAL;
        } else if (error.type === LoadError.TOKEN_REVOKED) {
            this.state = ManagerState.REVOKED_CREDENTIAL;
        } else {
            this.state = ManagerState.UNEXPECTED_ERROR_CREDENTIAL;
        }
    }

    @action
    createCredential(userName, password, requirePush: boolean) {
        this.pendingValidation = true;

        return this._api
            .createCredential(this.repositoryUrl, userName, password, this.branch, requirePush)
            .then(...delayBoth(MIN_DELAY))
            .then(response => this._createCredentialSuccess(response))
            .catch(error => this._onCreateCredentialFailure(error));
    }

    @action
    _createCredentialSuccess(ignored) {
        this.pendingValidation = false;
        this.state = ManagerState.SAVE_SUCCESS;

        return this.findExistingCredential();
    }

    @action
    _onCreateCredentialFailure(error) {
        this.pendingValidation = false;

        if (error.type === SaveError.INVALID_CREDENTIAL) {
            this.state = ManagerState.INVALID_CREDENTIAL;
        } else {
            throw error;
        }
    }
}
