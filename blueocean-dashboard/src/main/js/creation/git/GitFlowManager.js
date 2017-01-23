import React from 'react';
import { action, computed, observable } from 'mobx';
import { Promise } from 'es6-promise';

import { i18nTranslator } from '@jenkins-cd/blueocean-core-js';
const translate = i18nTranslator('blueocean-dashboard');

import FlowManager from '../flow2/FlowManager';
import GitConnectStep from './GitConnectStep';
import GitCompletedStep from './GitCompletedStep';
import GitRenameStep from './steps/GitRenameStep';
import FlowStatus from './GitCreationStatus';

// constants used to defined the special 'system ssh' key to ensure it's only created once, then reused.
const SYSTEM_SSH_ID = 'git-ssh-key-master';
const SYSTEM_SSH_DESCRIPTION = 'Master SSH Key for Git Creation';

/**
 * Impl of FlowManager for git creation flow.
 */
export default class GitFlowManager extends FlowManager {

    @observable
    creationStatus = null;

    @computed
    get isConnectEnabled() {
        return this.creationStatus !== FlowStatus.STEP_RENAME &&
                this.creationStatus !== FlowStatus.COMPLETE;
    }

    @computed
    get isRenameEnabled() {
        return this.creationStatus === FlowStatus.STEP_RENAME;
    }

    systemSSHCredential = null;

    pipelineName = null;

    credentialId = null;

    pipeline = null;

    constructor(createApi, credentialsApi) {
        super();

        this._createApi = createApi;
        this._credentialsApi = credentialsApi;
    }

    translate(key, opts) {
        return translate(key, opts);
    }

    getInitialStep() {
        return <GitConnectStep />;
    }

    onInitialized() {
        this.setPendingSteps([
            this.translate('creation.git.step3.title_default'),
        ]);
    }

    listAllCredentials() {
        return this._credentialsApi.listAllCredentials()
            .then(creds => this._prepareCredentialsList(creds));
    }

    createWithSSHKeyCredential(repositoryUrl, sshKey) {
        this._setStatus(FlowStatus.CREATE_CREDS);

        return this._credentialsApi.saveSSHKeyCredential(sshKey)
            .then(({ credentialId }) => (
                    this.createPipeline(repositoryUrl, credentialId)
                )
            );
    }

    createWithUsernamePasswordCredential(repositoryUrl, username, password) {
        this._setStatus(FlowStatus.CREATE_CREDS);

        return this._credentialsApi.saveUsernamePasswordCredential(username, password)
            .then(({ credentialId }) => (
                    this.createPipeline(repositoryUrl, credentialId)
                )
            );
    }

    createWithSystemSSHCredential(repositoryUrl) {
        // if the system ssh credential was created previously, we can proceed to creation immediately
        if (this.systemSSHCredential) {
            return this.createPipeline(repositoryUrl, this.systemSSHCredential.id);
        }

        // if it wasn't, then we need to create the cred, then use it in the creation
        return this._credentialsApi.saveSystemSSHCredential(SYSTEM_SSH_ID, SYSTEM_SSH_DESCRIPTION)
            .then(({ credentialId }) => (
                    this.createPipeline(repositoryUrl, credentialId)
                )
            );
    }

    checkPipelineNameAvailable(name) {
        if (!name) {
            return new Promise(resolve => resolve(false));
        }

        return this._createApi.checkPipelineNameAvailable(name);
    }

    createPipeline(repositoryUrl, credentialId) {
        this.repositoryUrl = repositoryUrl;
        this.credentialId = credentialId;
        this.pipelineName = this._createNameFromRepoUrl(repositoryUrl);
        return this._initiateCreatePipeline();
    }

    saveRenamedPipeline(pipelineName) {
        this.pipelineName = pipelineName;
        return this._initiateCreatePipeline();
    }

    _prepareCredentialsList(credentialList) {
        // find the special 'system ssh' credential if it was already created
        const systemSSH = credentialList
            .filter(item => item.id === SYSTEM_SSH_ID)
            .pop();

        if (systemSSH) {
            this.systemSSHCredential = systemSSH;
        }

        // remove 'system ssh' from the main list
        return credentialList
            .filter(item => item.id !== SYSTEM_SSH_ID);
    }

    _initiateCreatePipeline() {
        this._setStatus(FlowStatus.CREATE_PIPELINE);
        this.pushStep(<GitCompletedStep />);
        this.setPendingSteps();

        return this._createApi.createPipeline(this.repositoryUrl, this.credentialId, this.pipelineName)
            .then(pipeline => this._createPipelineSuccess(pipeline), error => this._createPipelineError(error));
    }

    @action
    _setStatus(status) {
        this.creationStatus = status;
    }

    @action
    _createPipelineSuccess(pipeline) {
        this._setStatus(FlowStatus.COMPLETE);
        this.pipeline = pipeline;
    }

    @action
    _createPipelineError() {
        this._setStatus(FlowStatus.STEP_RENAME);
        this.replaceCurrentStep(<GitRenameStep pipelineName={this.pipelineName} />);
        this.setPendingSteps([
            this.translate('creation.git.step3.title_default'),
        ]);
    }

    _createNameFromRepoUrl(repositoryUrl) {
        const lastSlashToken = repositoryUrl ? repositoryUrl.split('/').slice(-1).join('') : '';
        return lastSlashToken.split('.').slice(0, 1).join('');
    }

}
