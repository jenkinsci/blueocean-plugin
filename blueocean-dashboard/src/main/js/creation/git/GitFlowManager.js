import React from 'react';
import { action, observable } from 'mobx';

import FlowManager from '../flow2/FlowManager';
import GitConnectStep from './GitConnectStep';
import GitCompletedStep from './GitCompletedStep';
import FlowStatus from './GitCreationStatus';

export default class GitFlowManger extends FlowManager {

    @observable
    creationStatus = null;

    pipeline = null;

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
            .then(credentialId => (
                    this.createPipeline(repositoryUrl, credentialId)
                )
            );
    }

    // eslint-disable-next-line no-unused-vars
    createWithUsernamePasswordCredential(repositoryUrl, username, password) {
        return this.createWithSshKeyCredential();
    }

    // eslint-disable-next-line no-unused-vars
    createWithSystemSshCredential(repositoryUrl) {
        return this.createWithSshKeyCredential();
    }

    createPipeline(repositoryUrl, credentialId) {
        this.pushStep(<GitCompletedStep />);
        this.setPendingSteps([]);

        this._createApi.createPipeline(repositoryUrl, credentialId)
            .then(pipeline => this._setPipeline(pipeline));
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

}
