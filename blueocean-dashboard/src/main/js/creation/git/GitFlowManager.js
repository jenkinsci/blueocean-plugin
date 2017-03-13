import React from 'react';
import { action, computed, observable } from 'mobx';
import { Promise } from 'es6-promise';

import { i18nTranslator, logging } from '@jenkins-cd/blueocean-core-js';
const translate = i18nTranslator('blueocean-dashboard');

import FlowManager from '../flow2/FlowManager';
import waitAtLeast from '../flow2/waitAtLeast';
import { CreatePipelineOutcome } from './GitCreationApi';
import { CredentialsManager } from '../credentials/CredentialsManager';
import { UnknownErrorStep } from './steps/UnknownErrorStep';
import { LoadingStep } from './steps/LoadingStep';
import GitConnectStep from './GitConnectStep';
import GitCompletedStep from './GitCompletedStep';
import GitRenameStep from './steps/GitRenameStep';
import STATE from './GitCreationState';


const LOGGER = logging.logger('io.jenkins.blueocean.git-pipeline');
const MIN_DELAY = 500;
const SAVE_DELAY = 1000;


/**
 * Impl of FlowManager for git creation flow.
 */
export default class GitFlowManager extends FlowManager {

    credentialsManager = null;

    @observable
    noCredentialsOption = null;

    @computed
    get credentials() {
        const credentials = this.credentialsManager.displayedCredentials || [];
        return [].concat(this.noCredentialsOption, credentials);
    }

    @observable
    outcome = null;

    pipelineName = null;

    credentialId = null;

    pipeline = null;

    constructor(createApi, credentialsApi) {
        super();

        this._createApi = createApi;
        this.credentialsManager = new CredentialsManager(credentialsApi);
        this._initalize();
    }

    @action
    _initalize() {
        this.noCredentialsOption = {
            displayName: translate('creation.git.step1.credentials_placeholder'),
        };
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
        this.credentialId = credential && credential !== this.noCredentialsOption ? credential.id : null;
        this.pipelineName = this._createNameFromRepoUrl(repositoryUrl);
        return this._initiateCreatePipeline();
    }

    saveRenamedPipeline(pipelineName) {
        this.pipelineName = pipelineName;
        return this._initiateCreatePipeline();
    }

    _showPlaceholder() {
        this.setPlaceholders([
            this.translate('creation.git.step3.title_completed'),
        ]);
    }

    _showConnectStep() {
        this.renderStep({
            stateId: STATE.STEP_CONNECT,
            stepElement: <GitConnectStep />,
        });

        this._showPlaceholder();
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

        LOGGER.debug('creating pipeline with parameters', this.repositoryUrl, this.credentialId, this.pipelineName);

        return this._createApi.createPipeline(this.repositoryUrl, this.credentialId, this.pipelineName)
            .then(waitAtLeast(SAVE_DELAY))
            .then(result => this._createPipelineComplete(result));
    }

    @action
    _createPipelineComplete(result) {
        this.outcome = result.outcome;

        if (result.outcome === CreatePipelineOutcome.SUCCESS) {
            this.changeState(STATE.COMPLETE);
            this.pipeline = result.pipeline;
        } else if (result.outcome === CreatePipelineOutcome.INVALID_NAME) {
            this.renderStep({
                stateId: STATE.STEP_RENAME,
                stepElement: <GitRenameStep pipelineName={this.pipelineName} />,
                afterStateId: STATE.STEP_CONNECT,
            });
            this._showPlaceholder();
        } else if (result.outcome === CreatePipelineOutcome.INVALID_URI || result.outcome === CreatePipelineOutcome.INVALID_CREDENTIAL) {
            this.removeSteps({ afterStateId: STATE.STEP_CONNECT });
            this._showPlaceholder();
        } else if (result.outcome === CreatePipelineOutcome.ERROR) {
            this.renderStep({
                stateId: STATE.ERROR,
                stepElement: <UnknownErrorStep error={result.error} />,
                afterStateId: STATE.STEP_CONNECT,
            });
        }
    }

    _createNameFromRepoUrl(repositoryUrl) {
        const lastSlashToken = repositoryUrl ? repositoryUrl.split('/').slice(-1).join('') : '';
        return lastSlashToken.split('.').slice(0, 1).join('');
    }

}
