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

const SYSTEM_SSH_ID = 'git-ssh-key-master';
const SYSTEM_SSH_DESCRIPTION = 'Master SSH Key for Git Creation';

export default class GitFlowManger extends FlowManager {

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

    systemSshCredential = null;

    // TODO: eliminate this property if possible
    hasNameConflict = false;
    // TODO: eliminate this property if possible
    pipeline = null;

    pipelineName = null;

    credentialId = null;

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
            'Complete',
        ]);
    }

    listAllCredentials() {
        return this._credentialsApi.listAllCredentials()
            .then(creds => this._prepareCredentialsList(creds));
    }

    createWithSshKeyCredential(repositoryUrl, sshKey) {
        this._setStatus(FlowStatus.CREATE_CREDS);

        return this._credentialsApi.saveSshKeyCredential(sshKey)
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

    createWithSystemSshCredential(repositoryUrl) {
        if (this.systemSshCredential) {
            return this.createPipeline(repositoryUrl, this.systemSshCredential.id);
        }

        return this._credentialsApi.saveSystemSshCredential(SYSTEM_SSH_ID, SYSTEM_SSH_DESCRIPTION)
            .then(({ credentialId }) => (
                    this.createPipeline(repositoryUrl, credentialId)
                )
            );
    }

    checkPipelineNameAvailable(name) {
        if (!name) {
            return new Promise(() => false);
        }

        return this._createApi.checkPipelineNameAvailable(name);
    }

    createPipeline(repositoryUrl, credentialId) {
        this.repositoryUrl = repositoryUrl;
        this.credentialId = credentialId;
        return this._initiateCreatePipeline();
    }

    saveRenamedPipeline(pipelineName) {
        this.pipelineName = pipelineName;
        return this._initiateCreatePipeline();
    }

    _prepareCredentialsList(credentialList) {
        const systemSsh = credentialList
            .filter(item => item.id === SYSTEM_SSH_ID)
            .pop();

        if (systemSsh) {
            this.systemSshCredential = systemSsh;
        }

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
    _createPipelineError(error) {
        const { responseBody } = error;

        this._setStatus(FlowStatus.STEP_RENAME);

        if (this.hasNameConflict) {
            this.popStep();
        }

        this.replaceCurrentStep(<GitRenameStep pipelineError={responseBody.message} />);
        this.setPendingSteps(['Completed']);
        this.hasNameConflict = true;
    }

}
