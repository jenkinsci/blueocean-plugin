import React from 'react';
import { action, computed, observable } from 'mobx';
import { sseService } from '@jenkins-cd/blueocean-core-js';
import { logging } from '@jenkins-cd/blueocean-core-js';

import waitAtLeast from '../../flow2/waitAtLeast';

import FlowManager from '../../flow2/FlowManager';

import STATE from './BbCloudCreationState';

import { BbCredentialManager } from '../BbCredentialManager';
import { ListOrganizationsOutcome } from '../api/BbCreationApi';

import BbLoadingStep from '../steps/BbLoadingStep';
import BbCredentialsStep from '../steps/BbCredentialStep';
import BbOrgListStep from '../steps/BbOrgListStep';
import BbRepositoryStep from '../steps/BbRepositoryStep';
import BbCloudCompleteStep from './steps/BbCloudCompleteStep';
import BbUnknownErrorStep from '../steps/BbUnknownErrorStep';

const LOGGER = logging.logger('io.jenkins.blueocean.bitbucket-cloud-pipeline');
const MIN_DELAY = 500;
const FIRST_PAGE = 1;
const PAGE_SIZE = 100;
const SSE_TIMEOUT_DELAY = 1000 * 60;


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
        return this.stateId === STATE.PENDING_CREATION_SAVING ||
            this.stateId === STATE.STEP_COMPLETE_SAVING_ERROR ||
            this.stateId === STATE.PENDING_CREATION_EVENTS ||
            this.stateId === STATE.STEP_COMPLETE_EVENT_ERROR ||
            this.stateId === STATE.STEP_COMPLETE_EVENT_TIMEOUT ||
            this.stateId === STATE.STEP_COMPLETE_MISSING_JENKINSFILE ||
            this.stateId === STATE.STEP_COMPLETE_SUCCESS;
    }

    savedPipeline = null;

    _repositoryCache = {};

    _creationApi = null;

    _sseSubscribeId = null;

    _sseTimeoutId = null;

    constructor(creationApi, credentialsApi) {
        super();

        this._creationApi = creationApi;
        this.credentialManager = new BbCredentialManager(credentialsApi);
    }

    getApiUrl() {
        return 'https://bitbucket.org';
    }

    getStates() {
        return STATE.values();
    }

    getInitialStep() {
        return {
            stateId: STATE.PENDING_LOADING_CREDS,
            stepElement: <BbLoadingStep />,
        };
    }

    onInitialized() {
        this.findExistingCredential();
        this.setPlaceholders('Complete');
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
            });
        }
    }

    createCredential(userName, password) {
        return this.credentialManager.createCredential(this.getApiUrl(), userName, password)
            .then(success => this._createCredentialComplete(success));
    }

    _createCredentialComplete(response) {
        if (response.success) {
            this.renderStep({
                stateId: STATE.PENDING_LOADING_ORGANIZATIONS,
                stepElement: <BbLoadingStep />,
            });

            this.listOrganizations();
        }
    }

    @action
    listOrganizations() {
        this._creationApi.listOrganizations(this.credentialId, this.getApiUrl())
            .then(waitAtLeast(MIN_DELAY))
            .then(orgs => this._listOrganizationsSuccess(orgs));
    }

    @action
    _listOrganizationsSuccess(response) {
        if (response.outcome === ListOrganizationsOutcome.SUCCESS) {
            this.organizations = response.organizations;

            this.renderStep({
                stateId: STATE.STEP_CHOOSE_ORGANIZATION,
                stepElement: <BbOrgListStep />,
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
            stateId: STATE.PENDING_LOADING_REPOSITORIES,
            stepElement: <BbLoadingStep />,
        });
        this._loadAllRepositories(this.selectedOrganization);
    }

    @action
    selectRepository(repo) {
        this.selectedRepository = repo;
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
            .catch(error => console.log(error));
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
            });
        }
    }

    saveRepo() {
        this._saveRepo(this.selectedRepository.name);
    }

    @action
    _saveRepo(repoName) {
        this.renderStep({
            stateId: STATE.PENDING_CREATION_SAVING,
            stepElement: <BbCloudCompleteStep />,
        });

        this.setPlaceholders();

        this._initListeners();

        this._creationApi.createMbp(this.credentialId, this.getApiUrl(), this.selectedOrganization, repoName)
            .then(waitAtLeast(MIN_DELAY * 2))
            .then(r => this._saveRepoSuccess(r), e => this._saveRepoFailure(e));
    }

    @action
    _saveRepoSuccess(orgFolder) {
        LOGGER.debug(`org folder saved successfully: ${orgFolder.name}`);
        this.changeState(STATE.PENDING_CREATION_EVENTS);
        this.savedOrgFolder = orgFolder;
    }

    _saveRepoFailure() {
        LOGGER.error('org folder save failed!');
        this.changeState(STATE.STEP_COMPLETE_SAVING_ERROR);
    }


    _initListeners() {
        this._cleanupListeners();

        LOGGER.debug('listening for org folder and multibranch indexing events...');

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

        if (event.job_orgfolder_indexing_result === 'FAILURE') {
            this._finishListening(STATE.STEP_COMPLETE_EVENT_ERROR);
        }

        const pipelineFullName = `${this.selectedRepository.name}`;
        const multiBranchIndexingComplete = event.job_multibranch_indexing_result === 'SUCCESS' &&
            event.blueocean_job_pipeline_name === pipelineFullName;

        if (multiBranchIndexingComplete) {
            this._checkForSingleRepoCreation(pipelineFullName, multiBranchIndexingComplete, ({ isFound, pipeline }) => {
                if (isFound) {
                    this._completeSingleRepo(pipeline);
                } else {
                    this._finishListening(STATE.STEP_COMPLETE_MISSING_JENKINSFILE);
                }
            });
        }
    }

    /**
     * Complete creation process after an individual pipeline was created.
     * @param pipeline
     * @private
     */
    _completeSingleRepo(pipeline) {
        this.savedPipeline = pipeline;
        LOGGER.info(`creation succeeeded for ${pipeline.fullName}`);

        this._incrementPipelineCount();
        this._finishListening(STATE.STEP_COMPLETE_SUCCESS);
    }

    /**
     * Check for creation of a single pipeline within an org folder
     * @param {string} pipelineName
     * @param {boolean} multiBranchIndexingComplete
     * @private
     */
    _checkForSingleRepoCreation(pipelineName, multiBranchIndexingComplete, onComplete) {
        // if the multibranch pipeline has finished indexing,
        // we can aggressively check for the pipeline's existence to complete creation
        // if org indexing finished, be more conservative in the delay
        const delay = multiBranchIndexingComplete ? 500 : 5000;

        if (multiBranchIndexingComplete) {
            LOGGER.debug(`multibranch indexing for ${pipelineName} completed`);
        } else {
            LOGGER.debug('org folder indexing completed but no pipeline has been created (yet?)');
        }

        LOGGER.debug(`will check for single repo creation of ${pipelineName} in ${delay}ms`);

        setTimeout(() => {
            this._creationApi.findExistingOrgFolderPipeline(pipelineName)
                .then(data => {
                    LOGGER.debug(`check for pipeline complete. created? ${data.isFound}`);
                    onComplete(data);
                });
        }, delay);
    }

    _finishListening(stateId) {
        LOGGER.debug('finishListening', stateId);
        this.changeState(stateId);
        this._cleanupListeners();
    }

    @action
    _incrementPipelineCount() {
        this.pipelineCount++;
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

