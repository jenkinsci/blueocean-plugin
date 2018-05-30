import { action, observable } from 'mobx';

import { LoadError, SaveError } from '../bitbucket/BbCredentialsApi';

import { GitPWCredentialsApi } from './GitPWCredentialsApi';

import PromiseDelayUtils from '../../util/PromiseDelayUtils';

const MIN_DELAY = 500;
const { delayBoth } = PromiseDelayUtils;

export enum ManagerState {
    PENDING_LOADING_CREDS = 'PENDING_LOADING_CREDS',
    EXISTING_FOUND = 'EXISTING_FOUND',
    NEW_REQUIRED = 'NEW_REQUIRED',
    SAVE_SUCCESS = 'SAVE_SUCCESS',
    INVALID_CREDENTIAL = 'INVALID_CREDENTIAL',
    REVOKED_CREDENTIAL = 'REVOKED_CREDENTIAL',
    UNEXPECTED_ERROR_CREDENTIAL = 'UNEXPECTED_ERROR_CREDENTIAL',
    PENDING_VALIDATION = 'PENDING_VALIDATION',
}

export interface Credential {
    // FIXME: Canonical types in core-js
    id: string;
    displayName: string;
}

/**
 * Acts as a mobx store and api intermediary on behalf of GitCredentialsPickerPassword
 */
export class GitPWCredentialsManager {
    repositoryUrl?: string;
    branch: string = 'master';

    @observable state: ManagerState = ManagerState.PENDING_LOADING_CREDS;
    @observable existingCredential?: Credential;

    private api: GitPWCredentialsApi;

    constructor(api?: GitPWCredentialsApi) {
        this.api = api || new GitPWCredentialsApi();
    }

    configure(repositoryUrl: string, branch?: string) {
        this.repositoryUrl = repositoryUrl;

        if (typeof branch === 'string') {
            this.branch = branch;
        } else {
            this.branch = 'master';
        }
    }

    @action
    findExistingCredential() {
        this.state = ManagerState.PENDING_LOADING_CREDS;
        return this.api
            .findExistingCredential(this.repositoryUrl)
            .then(r => {
                return r;
            })
            .then(...delayBoth(MIN_DELAY))
            .then(r => {
                return r;
            })
            .then(
                action((credential: Credential) => {
                    this.state = ManagerState.EXISTING_FOUND;
                    this.existingCredential = credential;
                    return credential;
                })
            )
            .catch(
                action((error: any) => {
                    if (error.type === LoadError.TOKEN_NOT_FOUND) {
                        this.state = ManagerState.NEW_REQUIRED;
                    } else if (error.type === LoadError.TOKEN_INVALID) {
                        this.state = ManagerState.INVALID_CREDENTIAL;
                    } else if (error.type === LoadError.TOKEN_REVOKED) {
                        this.state = ManagerState.REVOKED_CREDENTIAL;
                    } else {
                        this.state = ManagerState.UNEXPECTED_ERROR_CREDENTIAL;
                    }
                })
            );
    }

    @action
    createCredential(userName, password, requirePush: boolean) {
        this.state = ManagerState.PENDING_VALIDATION;
        this.existingCredential = undefined;

        const repositoryUrl = this.repositoryUrl;
        const branchName = this.branch;

        return this.api
            .createCredential(repositoryUrl, userName, password, branchName, requirePush)
            .then(...delayBoth(MIN_DELAY))
            .then(
                action(() => {
                    // Need to look up the existing credential because create service doesn't return the new id
                    // Need to use _api directly rather than this.findExistingCredential because we don't want
                    // the state to change until it's done
                    return this.api.findExistingCredential(repositoryUrl);
                })
            )
            .then(
                action((credential: Credential) => {
                    this.state = ManagerState.SAVE_SUCCESS;
                    this.existingCredential = credential;
                    return credential;
                })
            )
            .catch(
                action((error: any) => {
                    if (error.type === SaveError.INVALID_CREDENTIAL) {
                        this.state = ManagerState.INVALID_CREDENTIAL;
                    } else {
                        this.state = ManagerState.UNEXPECTED_ERROR_CREDENTIAL;
                    }
                    throw error;
                })
            );
    }
}
