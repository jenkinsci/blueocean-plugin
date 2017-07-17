import React from 'react';
import { action, computed, observable } from 'mobx';
import { sseService } from '@jenkins-cd/blueocean-core-js';
import { logging, i18nTranslator } from '@jenkins-cd/blueocean-core-js';
import waitAtLeast from '../../flow2/waitAtLeast';

import FlowManager from '../../flow2/FlowManager';

import STATE from './BbCloudCreationState';

import { BbCredentialManager } from '../BbCredentialManager';
import { ListOrganizationsOutcome } from '../api/BbCreationApi';
import { CreateMbpOutcome } from '../api/BbCreationApi';

import BbLoadingStep from '../steps/BbLoadingStep';
import BbCredentialsStep from '../steps/BbCredentialStep';
import BbOrgListStep from '../steps/BbOrgListStep';
import BbRepositoryStep from '../steps/BbRepositoryStep';
import BbCloudCompleteStep from './steps/BbCloudCompleteStep';
import BbUnknownErrorStep from '../steps/BbUnknownErrorStep';
import BbRenameStep from '../steps/BbRenameStep';

const LOGGER = logging.logger('io.jenkins.blueocean.bitbucket-cloud-pipeline');
const MIN_DELAY = 500;
const FIRST_PAGE = 1;
const PAGE_SIZE = 100;
const SSE_TIMEOUT_DELAY = 1000 * 60;
const translate = i18nTranslator('blueocean-dashboard');

export default class BbCloudFlowManager extends FlowManager {

    credentialManager = null;
    queuedIndexAllocations = 0;

    get credentialId() {
        return this.credentialManager && this.credentialManager.credentialId;
    }

    @observable
    organizations = [];

    @observable
    repositories = [];

    @observable
    repositoriesLoading = false;

    @computed get selectableRepositories() {
        if (!this.repositories) {
            return [];
        }
        return this.repositories;
    }

    @observable
    selectedOrganization = null;

    @observable
    selectedRepository = null;

    @computed get stepsDisabled() {
        return this.stateId === STATE.STEP_COMPLETE_SAVING_ERROR ||
            this.stateId === STATE.STEP_COMPLETE_EVENT_ERROR ||
            this.stateId === STATE.STEP_COMPLETE_EVENT_TIMEOUT ||
            this.stateId === STATE.STEP_COMPLETE_MISSING_JENKINSFILE ||
            this.stateId === STATE.STEP_COMPLETE_SUCCESS;
    }

    pipeline = null;

    _repositoryCache = {};

    _creationApi = null;

    _sseSubscribeId = null;

    _sseTimeoutId = null;

    constructor(creationApi, credentialsApi) {
        super();
        this._creationApi = creationApi;
        this.credentialManager = new BbCredentialManager(credentialsApi);
    }

    translate(key, opts) {
        return translate(key, opts);
    }

    getApiUrl() {
        return 'https://bitbucket.org';
    }

    getStates() {
        return STATE.values();
    }

    getState() {
        return STATE;
    }

    getInitialStep() {
        return {
            stateId: STATE.PENDING_LOADING_CREDS,
            stepElement: <BbLoadingStep />,
        };
    }

    onInitialized() {
        this.findExistingCredential();
        this.setPlaceholders(translate('creation.core.status.completed'));
    }

    destroy() {
        this._cleanupListeners();
    }

    findExistingCredential() {
        return this.credentialManager.findExistingCredential(this.getApiUrl())
            .then(waitAtLeast(MIN_DELAY))
            .then(success => this._findExistingCredentialComplete(success));
    }

    _findExistingCredentialComplete(success) {
        if (success) {
            this.changeState(STATE.PENDING_LOADING_ORGANIZATIONS);
            this.listOrganizations();
        } else {
            this.renderStep({
                stateId: STATE.STEP_CREDENTIAL,
                stepElement: <BbCredentialsStep />,
                afterStateId: this._getCredentialsStepAfterStateId(),
            });
        }
    }

    _getCredentialsStepAfterStateId() {
        return null;
    }
    createCredential(userName, password) {
        return this.credentialManager.createCredential(this.getApiUrl(), userName, password)
            .then(success => this._createCredentialComplete(success));
    }

    _createCredentialComplete(response) {
        if (response.success) {
            this._renderLoadingOrganizations();
        }
    }

    _renderLoadingOrganizations() {
        this.renderStep({
            stateId: STATE.PENDING_LOADING_ORGANIZATIONS,
            stepElement: <BbLoadingStep />,
            afterStateId: STATE.STEP_CREDENTIAL,
        });

        this.listOrganizations();
    }

    checkPipelineNameAvailable(name) {
        if (!name) {
            return new Promise(resolve => resolve(false));
        }

        return this._creationApi.checkPipelineNameAvailable(name);
    }

    @action
    listOrganizations() {
        this._creationApi.listOrganizations(this.credentialId, this.getApiUrl())
            .then(waitAtLeast(MIN_DELAY))
            .then(orgs => this._listOrganizationsSuccess(orgs));
    }

    _getOrganizationsStepAfterStateId() {
        return this.isStateAdded(STATE.STEP_CREDENTIAL) ?
            STATE.STEP_CREDENTIAL : null;
    }

    @action
    _listOrganizationsSuccess(response) {
        if (response.outcome === ListOrganizationsOutcome.SUCCESS) {
            this.organizations = response.organizations;

            this.renderStep({
                stateId: STATE.STEP_CHOOSE_ORGANIZATION,
                stepElement: <BbOrgListStep />,
                afterStateId: this._getOrganizationsStepAfterStateId(),
            });
        } else if (response.outcome === ListOrganizationsOutcome.INVALID_CREDENTIAL_ID) {
            this.organizations = response.organizations;

            this.renderStep({
                stateId: STATE.STEP_CREDENTIAL,
                stepElement: <BbCredentialsStep />,
            });
        } else {
            this.renderStep({
                stateId: STATE.ERROR_UNKOWN,
                stepElement: <BbUnknownErrorStep message={response.error} />,
            });
        }
    }

    @action
    selectOrganization(organization) {
        this.selectedOrganization = organization;
        this.renderStep({
            stateId: STATE.PENDING_LOADING_ORGANIZATIONS,
            stepElement: <BbLoadingStep />,
            afterStateId: STATE.STEP_CHOOSE_ORGANIZATION,
        });
        this._loadAllRepositories(this.selectedOrganization);
    }

    @action
    selectRepository(repo) {
        this.selectedRepository = repo;
        this.pipelineName = this.selectedRepository.name;
    }

    @action
    _loadAllRepositories(organization) {
        this.repositories.replace([]);
        this.repositoriesLoading = true;

        let promise = null;
        const cachedRepos = this._repositoryCache[organization.key];

        if (cachedRepos) {
            promise = new Promise(resolve => resolve({ repositories: { items: cachedRepos } }));
        } else {
            promise = this._loadPagedRepository(organization.key, FIRST_PAGE);
        }

        promise
            .then(waitAtLeast(MIN_DELAY))
            .then(repos => this._updateRepositories(organization.key, repos))
            .catch(error => new BbUnknownErrorStep(error));
    }

    _loadPagedRepository(organizationName, pageNumber, pageSize = PAGE_SIZE) {
        return this._creationApi.listRepositories(this.credentialId, this.getApiUrl(), organizationName, pageNumber, pageSize);
    }

    @action
    _updateRepositories(organizationName, repoData) {
        const { items, nextPage } = repoData.repositories;
        const firstPage = this.repositories.length === 0;
        const morePages = !isNaN(parseInt(nextPage, 10));

        this.repositories.push(...items);
        this._repositoryCache[organizationName] = this.repositories.slice();

        if (morePages) {
            this._loadPagedRepository(organizationName, nextPage)
                .then(repos2 => this._updateRepositories(organizationName, repos2, nextPage));
        } else {
            this.repositoriesLoading = false;
        }
        if (firstPage) {
            // render the repo list only once, after the first page comes back
            // otherwise we'll lose step's internal state
            this.renderStep({
                stateId: STATE.STEP_CHOOSE_REPOSITORY,
                stepElement: <BbRepositoryStep />,
                afterStateId: STATE.STEP_CHOOSE_ORGANIZATION,
            });
        }
    }

    saveRepo() {
        this._saveRepo();
    }

    @action
    _saveRepo() {
        const afterStateId = this.isStateAdded(STATE.STEP_RENAME) ?
            STATE.STEP_RENAME : STATE.STEP_CHOOSE_REPOSITORY;

        this.renderStep({
            stateId: STATE.STEP_COMPLETE_SUCCESS,
            stepElement: <BbCloudCompleteStep />,
            afterStateId,
        });

        this.setPlaceholders();

        this._initListeners();

        this._creationApi.createMbp(this.credentialId, this.getApiUrl(), this.pipelineName, this.selectedOrganization.key, this.selectedRepository.name)
            .then(waitAtLeast(MIN_DELAY * 2))
            .then(result => this._createPipelineComplete(result));
    }

    saveRenamedPipeline(pipelineName) {
        this.pipelineName = pipelineName;
        return this._saveRepo();
    }

    @action
    _createPipelineComplete(result) {
        this.outcome = result.outcome;
        if (result.outcome === CreateMbpOutcome.SUCCESS) {
            this.changeState(STATE.STEP_COMPLETE_SUCCESS);
            this.pipeline = result.pipeline;
        } else if (result.outcome === CreateMbpOutcome.INVALID_NAME) {
            this.renderStep({
                stateId: STATE.STEP_RENAME,
                stepElement: <BbRenameStep pipelineName={this.pipelineName} />,
                afterStateId: STATE.STEP_CHOOSE_REPOSITORY,
            });
            this._showPlaceholder();
        } else if (result.outcome === CreateMbpOutcome.INVALID_URI || result.outcome === CreateMbpOutcome.INVALID_CREDENTIAL) {
            this.removeSteps({ afterStateId: STATE.STEP_CREDENTIAL });
            this._showPlaceholder();
        } else if (result.outcome === CreateMbpOutcome.ERROR) {
            const afterStateId = this.isStateAdded(STATE.STEP_RENAME) ?
                STATE.STEP_RENAME : STATE.STEP_CHOOSE_REPOSITORY;
            this.renderStep({
                stateId: STATE.ERROR,
                stepElement: <BbUnknownErrorStep error={result.error} />,
                afterStateId,
            });
        }
    }

    _showPlaceholder() {
        this.setPlaceholders([
            this.translate('creation.core.status.completed'),
        ]);
    }

    _initListeners() {
        this._cleanupListeners();

        LOGGER.debug('listening for org folder and multi-branch indexing events...');

        this._sseSubscribeId = sseService.registerHandler(event => this._onSseEvent(event));
        this._sseTimeoutId = setTimeout(() => {
            this._onSseTimeout();
        }, SSE_TIMEOUT_DELAY);
    }

    _cleanupListeners() {
        if (this._sseSubscribeId || this._sseTimeoutId) {
            LOGGER.debug('cleaning up existing SSE listeners');
        }

        if (this._sseSubscribeId) {
            sseService.removeHandler(this._sseSubscribeId);
            this._sseSubscribeId = null;
        }
        if (this._sseTimeoutId) {
            clearTimeout(this._sseTimeoutId);
            this._sseTimeoutId = null;
        }
    }

    _onSseEvent(event) {
        if (LOGGER.isDebugEnabled()) {
            this._logEvent(event);
        }

        if (event.job_multibranch_indexing_status === 'INDEXING' && event.job_run_status === 'QUEUED') {
            this.queuedIndexAllocations++;
        }

        if (event.job_multibranch_indexing_result) {
            this.queuedIndexAllocations--;
        }

        const multiBranchIndexingComplete = event.job_multibranch_indexing_result === 'SUCCESS' &&
            event.blueocean_job_pipeline_name === this.pipelineName;

        if (multiBranchIndexingComplete) {
            this.savedPipeline = this.pipelineName;
            LOGGER.info(`creation succeeded for ${this.pipelineName}`);
            this._finishListening(STATE.STEP_COMPLETE_SUCCESS);
        }
    }

    _finishListening(stateId) {
        LOGGER.debug('finishListening', stateId);
        this.changeState(stateId);
        this._cleanupListeners();
    }

    _onSseTimeout() {
        LOGGER.debug(`wait for events timed out after ${SSE_TIMEOUT_DELAY}ms`);
        this.changeState(STATE.STEP_COMPLETE_EVENT_TIMEOUT);
        this._cleanupListeners();
    }

    _logEvent(event) {
        if (event.job_multibranch_indexing_result === 'SUCCESS' || event.job_multibranch_indexing_result === 'FAILURE') {
            LOGGER.debug(`indexing for ${event.blueocean_job_pipeline_name} finished: ${event.job_multibranch_indexing_result}`);
        } else if (event.jenkins_event === 'job_crud_created') {
            // LOGGER.debug(`created branch: ${event.job_name}`);
        }
    }
}

