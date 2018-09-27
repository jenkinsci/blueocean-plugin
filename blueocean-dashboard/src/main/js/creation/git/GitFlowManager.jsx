import React from 'react';
import { action, computed, observable } from 'mobx';
import Promise from 'bluebird';

import { i18nTranslator, logging, sseService, pipelineService } from '@jenkins-cd/blueocean-core-js';
const translate = i18nTranslator('blueocean-dashboard');

import FlowManager from '../CreationFlowManager';
import waitAtLeast from '../flow2/waitAtLeast';
import { CreatePipelineOutcome } from './GitCreationApi';
import { CredentialsManager } from '../credentials/CredentialsManager';
import { UnknownErrorStep } from './steps/UnknownErrorStep';
import { LoadingStep } from './steps/LoadingStep';
import GitConnectStep, { isSshRepositoryUrl } from './GitConnectStep';
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

    @observable noCredentialsOption = null;

    @computed
    get credentials() {
        const credentials = this.credentialsManager.credentials.slice();
        return [].concat(this.noCredentialsOption, credentials);
    }

    @observable outcome = null;

    pipelineName = null;

    selectedCredential = null;

    pipeline = null;

    _sseSubscribeId = null;

    constructor(createApi, credentialsApi) {
        super();

        this._createApi = createApi;
        this.credentialsManager = new CredentialsManager(credentialsApi);
        this._initialize();
    }

    @action
    _initialize() {
        this._sseSubscribeId = sseService.registerHandler(event => this._onSseEvent(event));
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
        return this.credentialsManager
            .listAllCredentials()
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
        this.selectedCredential = credential;
        this.pipelineName = this._createNameFromRepoUrl(repositoryUrl);
        return this._initiateCreatePipeline();
    }

    saveRenamedPipeline(pipelineName) {
        this.pipelineName = pipelineName;
        return this._initiateCreatePipeline();
    }

    _showPlaceholder() {
        this.setPlaceholders([this.translate('creation.git.step3.title_completed')]);
    }

    _showConnectStep() {
        this.renderStep({
            stateId: STATE.STEP_CONNECT,
            stepElement: <GitConnectStep />,
        });

        this._showPlaceholder();
    }

    _initiateCreatePipeline() {
        const afterStateId = this.isStateAdded(STATE.STEP_RENAME) ? STATE.STEP_RENAME : STATE.STEP_CONNECT;

        this.renderStep({
            stateId: STATE.CREATE_PIPELINE,
            stepElement: <GitCompletedStep />,
            afterStateId,
        });

        this.setPlaceholders();

        let credentialId = null;

        if (this.selectedCredential !== this.noCredentialsOption) {
            credentialId = this.selectedCredential.id;
        }

        LOGGER.debug('creating pipeline with parameters', this.repositoryUrl, credentialId, this.pipelineName);

        return this._createApi
            .createPipeline(this.repositoryUrl, credentialId, this.pipelineName)
            .then(waitAtLeast(SAVE_DELAY))
            .then(result => this._createPipelineComplete(result));
    }

    @action
    _createPipelineComplete(result) {
        this.outcome = result.outcome;

        if (result.outcome === CreatePipelineOutcome.SUCCESS) {
            this.pipeline = result.pipeline;
            // wait for SSE events - need to finish indexing
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

    _isHttpRepositoryUrl(repositoryUrl) {
        const url = (repositoryUrl && repositoryUrl.toLowerCase()) || '';
        return url.indexOf('http') === 0 || url.indexOf('https') === 0;
    }

    _createNameFromRepoUrl(repositoryUrl) {
        const lastSlashToken = repositoryUrl
            ? repositoryUrl
                  .split('/')
                  .slice(-1)
                  .join('')
            : '';
        return lastSlashToken
            .split('.')
            .slice(0, 1)
            .join('');
    }

    _cleanupListeners() {
        if (this._sseSubscribeId) {
            sseService.removeHandler(this._sseSubscribeId);
            this._sseSubscribeId = null;
        }
    }

    _finishListening(state) {
        this.changeState(state);
        this._cleanupListeners();
    }

    _onSseEvent(event) {
        if (LOGGER.isDebugEnabled()) {
            this._logEvent(event);
        }

        if (event.blueocean_job_pipeline_name !== this.pipelineName) {
            return;
        }

        if (
            event.blueocean_job_pipeline_name === this.pipelineName &&
            event.jenkins_object_type === 'org.jenkinsci.plugins.workflow.job.WorkflowRun' &&
            (event.job_run_status === 'ALLOCATED' ||
                event.job_run_status === 'RUNNING' ||
                event.job_run_status === 'SUCCESS' ||
                event.job_run_status === 'FAILURE')
        ) {
            // set pipeline details thats needed later on in BbCompleteStep.navigatePipeline()
            this.pipeline = { organization: event.jenkins_org, fullName: this.pipelineName };
            this._finishListening(STATE.STEP_COMPLETE_SUCCESS);
            return;
        }

        if (event.job_multibranch_indexing_result) {
            pipelineService
                .fetchPipeline(event.blueocean_job_rest_url, { useCache: false })
                .then(pipeline => {
                    if (!pipeline.branchNames.length && isSshRepositoryUrl(this.repositoryUrl)) {
                        this._finishListening(STATE.STEP_COMPLETE_MISSING_JENKINSFILE);
                    } else {
                        this._finishListening(STATE.STEP_COMPLETE_SUCCESS);
                    }
                })
                .catch(() => {
                    this._finishListening(STATE.STEP_COMPLETE_EVENT_ERROR);
                });
        }
    }
}
