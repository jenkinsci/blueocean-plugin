import React from 'react';
import { action, computed } from 'mobx';
import { Promise } from 'es6-promise';
import waitAtLeast from '../flow2/waitAtLeast';

import { i18nTranslator } from '@jenkins-cd/blueocean-core-js';
const translate = i18nTranslator('blueocean-dashboard');

import FlowManager from '../flow2/FlowManager';
import { CredentialsManager } from '../credentials/CredentialsManager';
import { LoadingStep } from './steps/LoadingStep';
import GitConnectStep from './GitConnectStep';
import GitCompletedStep from './GitCompletedStep';
import GitRenameStep from './steps/GitRenameStep';
import STATE from './GitCreationState';

const MIN_DELAY = 500;


/**
 * Impl of FlowManager for git creation flow.
 */
export default class GitFlowManager extends FlowManager {

    credentialsManager = null;

    @computed
    get credentials() {
        return this.credentialsManager.displayedCredentials || [];
    }

    @computed
    get isConnectEnabled() {
        return this.stateId !== STATE.STEP_RENAME &&
                this.stateId !== STATE.COMPLETE;
    }

    @computed
    get isRenameEnabled() {
        return this.stateId === STATE.STEP_RENAME;
    }

    pipelineName = null;

    credentialId = null;

    pipeline = null;

    constructor(createApi, credentialsApi) {
        super();

        this._createApi = createApi;
        this.credentialsManager = new CredentialsManager(credentialsApi);
    }

    translate(key, opts) {
        return translate(key, opts);
    }

    getStates() {
        return STATE.values();
    }

    getInitialStep() {
        return {
            stateId: STATE.LOADING_CREDENTIALS,
            stepElement: <LoadingStep />,
        };
    }

    onInitialized() {
        this.listAllCredentials();
    }

    listAllCredentials() {
        return this.credentialsManager.listAllCredentials()
            .then(waitAtLeast(MIN_DELAY))
            .then(() => this._showConnectStep());
    }

    checkPipelineNameAvailable(name) {
        if (!name) {
            return new Promise(resolve => resolve(false));
        }

        return this._createApi.checkPipelineNameAvailable(name);
    }

    createPipeline(repositoryUrl, credential) {
        this.repositoryUrl = repositoryUrl;
        this.credentialId = credential ? credential.credentialId : null;
        this.pipelineName = this._createNameFromRepoUrl(repositoryUrl);
        return this._initiateCreatePipeline();
    }

    saveRenamedPipeline(pipelineName) {
        this.pipelineName = pipelineName;
        return this._initiateCreatePipeline();
    }

    _showPlaceholder() {
        this.setPlaceholders([
            this.translate('creation.git.step3.title_default'),
        ]);
    }

    _showConnectStep() {
        this.renderStep({
            stateId: STATE.STEP_CONNECT,
            stepElement: <GitConnectStep />,
        });

        this._showPlaceholder();
    }

    _showCreateCredsStep() {
        this.renderStep({
            stateId: STATE.CREATE_CREDS,
            stepElement: <GitCompletedStep />,
            afterStateId: STATE.STEP_CONNECT,
        });
    }

    _initiateCreatePipeline() {
        const afterStateId = this.isStateAdded(STATE.STEP_RENAME) ?
            STATE.STEP_RENAME : STATE.STEP_CONNECT;

        this.renderStep({
            stateId: STATE.CREATE_PIPELINE,
            stepElement: <GitCompletedStep />,
            afterStateId,
        });

        this.setPlaceholders();

        return this._createApi.createPipeline(this.repositoryUrl, this.credentialId, this.pipelineName)
            .then(pipeline => this._createPipelineSuccess(pipeline), error => this._createPipelineError(error));
    }

    @action
    _createPipelineSuccess(pipeline) {
        this.changeState(STATE.COMPLETE);
        this.pipeline = pipeline;
    }

    @action
    _createPipelineError() {
        this.renderStep({
            stateId: STATE.STEP_RENAME,
            stepElement: <GitRenameStep pipelineName={this.pipelineName} />,
            afterStateId: STATE.STEP_CONNECT,
        });
        this.setPlaceholders([
            this.translate('creation.git.step3.title_default'),
        ]);
    }

    _createNameFromRepoUrl(repositoryUrl) {
        const lastSlashToken = repositoryUrl ? repositoryUrl.split('/').slice(-1).join('') : '';
        return lastSlashToken.split('.').slice(0, 1).join('');
    }

}
