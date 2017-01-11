import React from 'react';
import { action, observable } from 'mobx';

import FlowManager from '../flow2/FlowManager';
import GitConnectStep from './GitConnectStep';
import GitCompletedStep from './GitCompletedStep';
import GitRenameStep from './steps/GitRenameStep';
import FlowStatus from './GitCreationStatus';

const SYSTEM_SSH_ID = 'github-ssh-key-master';
const SYSTEM_SSH_DESCRIPTION = 'Master SSH Key for Git Creation';

export default class GitFlowManger extends FlowManager {

    @observable
    creationStatus = null;

    systemSshCredential = null;

    hasNameConflict = false;

    pipeline = null;

    pipelineName = null;

    constructor(createApi, credentialsApi) {
        super();

        this._createApi = createApi;
        this._credentialsApi = credentialsApi;
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
            .then(pipeline => this._setPipeline(pipeline), error => this._pipelineError(error));
    }

    @action
    _setStatus(status) {
        this.creationStatus = status;
    }

    @action
    _setPipeline(pipeline) {
        this._setStatus(FlowStatus.COMPLETE);
        this.pipeline = pipeline;
    }

    @action
    _pipelineError(error) {
        const { responseBody } = error;

        this._setStatus(FlowStatus.NAME_CONFLICT);

        if (this.hasNameConflict) {
            this.popStep();
        }

        this.replaceCurrentStep(<GitRenameStep pipelineError={responseBody.message} />);
        this.setPendingSteps(['Completed']);
        this.hasNameConflict = true;
    }

}
