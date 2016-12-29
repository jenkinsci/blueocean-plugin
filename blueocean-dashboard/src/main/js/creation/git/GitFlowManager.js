import React from 'react';
import { action, observable } from 'mobx';

import FlowManager from '../flow2/FlowManager';
import GitConnectStep from './GitConnectStep';
import GitCompletedStep from './GitCompletedStep';
import GitRenameStep from './steps/GitRenameStep';
import FlowStatus from './GitCreationStatus';

export default class GitFlowManger extends FlowManager {

    @observable
    creationStatus = null;

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
        return this._credentialsApi.listAllCredentials();
    }

    createWithSshKeyCredential(repositoryUrl, sshKey) {
        this._setStatus(FlowStatus.CREATE_CREDS);

        return this._credentialsApi.saveSshKeyCredential(sshKey)
            .then(({ credentialId }) => (
                    this.createPipeline(repositoryUrl, credentialId)
                )
            );
    }

    // eslint-disable-next-line no-unused-vars
    createWithUsernamePasswordCredential(repositoryUrl, username, password) {
        this._setStatus(FlowStatus.CREATE_CREDS);

        return this._credentialsApi.saveUsernamePasswordCredential(username, password)
            .then(({ credentialId }) => (
                    this.createPipeline(repositoryUrl, credentialId)
                )
            );
    }

    // eslint-disable-next-line no-unused-vars
    createWithSystemSshCredential(repositoryUrl) {
        return;
        // return this.createWithSshKeyCredential();
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
