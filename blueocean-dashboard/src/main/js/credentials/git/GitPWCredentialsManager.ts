import {action, observable} from "mobx";

import {
    LoadError,
    SaveError,
} from '../bitbucket/BbCredentialsApi';

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

/**
 * Acts as a mobx store and api intermediary on behalf of GitCredentialsPickerPassword
 */
export class GitPWCredentialsManager {
    // TODO: Unit tests

    repositoryUrl?: string;
    branch: string = 'master';
    @observable state: ManagerState = ManagerState.PENDING_LOADING_CREDS;

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
            .catch(action((error: any) => {
                if (error.type === LoadError.TOKEN_NOT_FOUND) {
                    this.state = ManagerState.NEW_REQUIRED;
                } else if (error.type === LoadError.TOKEN_INVALID) {
                    this.state = ManagerState.INVALID_CREDENTIAL;
                } else if (error.type === LoadError.TOKEN_REVOKED) {
                    this.state = ManagerState.REVOKED_CREDENTIAL;
                } else {
                    this.state = ManagerState.UNEXPECTED_ERROR_CREDENTIAL;
                }
            }));
    }

    @action
    createCredential(userName, password, requirePush: boolean) {
        this.state = ManagerState.PENDING_VALIDATION;

        return this._api
            .createCredential(this.repositoryUrl, userName, password, this.branch, requirePush)
            .then(...delayBoth(MIN_DELAY))
            .then(action(() => {
                this.state = ManagerState.SAVE_SUCCESS;
                return this.findExistingCredential(); // Because create service doesn't return the new id
            }))
            .catch(action((error: any) => {
                if (error.type === SaveError.INVALID_CREDENTIAL) {
                    this.state = ManagerState.INVALID_CREDENTIAL;
                } else {
                    this.state = ManagerState.UNEXPECTED_ERROR_CREDENTIAL;
                    throw error;
                }
            }));
    }
}
