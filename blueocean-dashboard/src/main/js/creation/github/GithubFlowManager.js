import React from 'react';
import { action, computed, observable } from 'mobx';
import { sseService } from '@jenkins-cd/blueocean-core-js';
import { logging } from '@jenkins-cd/blueocean-core-js';

import waitAtLeast from '../flow2/waitAtLeast';

import FlowManager from '../flow2/FlowManager';

import STATE from './GithubCreationState';

import { ListOrganizationsOutcome } from './api/GithubCreationApi';

import GithubAlreadyDiscoverStep from './steps/GithubAlreadyDiscoverStep';
import GithubLoadingStep from './steps/GithubLoadingStep';
import GithubCredentialsStep from './steps/GithubCredentialStep';
import GithubInvalidOrgFolderStep from './steps/GithubInvalidOrgFolderStep';
import GithubOrgListStep from './steps/GithubOrgListStep';
import GithubChooseDiscoverStep from './steps/GithubChooseDiscoverStep';
import GithubConfirmDiscoverStep from './steps/GithubConfirmDiscoverStep';
import GithubRepositoryStep from './steps/GithubRepositoryStep';
import GithubCompleteStep from './steps/GithubCompleteStep';
import GithubUnknownErrorStep from './steps/GithubUnknownErrorStep';

const LOGGER = logging.logger('io.jenkins.blueocean.github-pipeline');
const MIN_DELAY = 500;
const FIRST_PAGE = 1;
const PAGE_SIZE = 100;
const SSE_TIMEOUT_DELAY = 1000 * 60;


export default class GithubFlowManager extends FlowManager {

    queuedIndexAllocations = 0;

    apiUrl = null;

    credentialId = null;

    credentialSelected = false;

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

        if (!this.existingOrgFolder || !this.existingOrgFolder.pipelineFolderNames) {
            return this.repositories;
        }

        // return repositories that are not already created as pipelines
        return this.repositories.filter(repo => (
            this.existingOrgFolder.pipelineFolderNames.indexOf(repo.name) === -1
        ));
    }

    @observable
    selectedOrganization = null;

    @observable
    existingOrgFolder = null;

    @computed get existingAutoDiscover() {
        return this.existingOrgFolder && this.existingOrgFolder.scanAllRepos;
    }

    @computed get existingPipelineCount() {
        return this.existingOrgFolder && this.existingOrgFolder.pipelineFolderNames && this.existingOrgFolder.pipelineFolderNames.length || 0;
    }

    @observable
    selectedRepository = null;

    @observable
    selectedAutoDiscover = null;

    @observable
    pipelineCount = 0;

    @computed get stepsDisabled() {
        return this.stateId === STATE.PENDING_CREATION_SAVING ||
            this.stateId === STATE.STEP_COMPLETE_SAVING_ERROR ||
            this.stateId === STATE.PENDING_CREATION_EVENTS ||
            this.stateId === STATE.STEP_COMPLETE_EVENT_ERROR ||
            this.stateId === STATE.STEP_COMPLETE_EVENT_TIMEOUT ||
            this.stateId === STATE.STEP_COMPLETE_MISSING_JENKINSFILE ||
            this.stateId === STATE.STEP_COMPLETE_SUCCESS;
    }

    savedOrgFolder = null;

    savedPipeline = null;

    _repositoryCache = {};

    _creationApi = null;

    _sseSubscribeId = null;

    _sseTimeoutId = null;

    constructor(creationApi) {
        super();

        this._creationApi = creationApi;
    }

    getStates() {
        return STATE.values();
    }

    getInitialStep() {
        return {
            stateId: STATE.PENDING_LOADING_CREDS,
            stepElement: <GithubLoadingStep />,
        };
    }

    onInitialized() {
        this._renderCredentialsStep();
        this.setPlaceholders('Complete');
    }

    destroy() {
        this._cleanupListeners();
    }

    getScmId() {
        return 'github';
    }

    getApiUrl() {
        // backend will default to api.github.com
        return null;
    }

    /**
     * stateId of the step after which the 'GithubCredentialsStep' should be added
     * @returns {string}
     * @private
     */
    _getCredentialsStepAfterStateId() {
        // the credentials step is always added at the beginning
        return null;
    }

    _renderCredentialsStep() {
        this.renderStep({
            stateId: STATE.STEP_ACCESS_TOKEN,
            stepElement: <GithubCredentialsStep
                scmId={this.getScmId()}
                onCredentialSelected={(cred, selectionType) => this._onCredentialSelected(cred, selectionType)}
            />,
            afterStateId: this._getCredentialsStepAfterStateId(),
        });
    }

    _onCredentialSelected(credential, selectionType) {
        this.credentialId = credential.credentialId;
        this.credentialSelected = selectionType === 'userSelected';
        this._renderLoadingOrganizations();
    }

    /**
     * stateId of the step after which the 'GithubOrgListStep' should be added
     * @returns {string}
     * @private
     */
    _getOrganizationsStepAfterStateId() {
        // if the credential was manually selected, add the organizations step after it
        // if auto-selected, just replace it altogether
        return this.credentialSelected ?
            STATE.STEP_ACCESS_TOKEN : null;
    }

    _renderLoadingOrganizations() {
        this.renderStep({
            stateId: STATE.PENDING_LOADING_ORGANIZATIONS,
            stepElement: <GithubLoadingStep />,
            afterStateId: this._getOrganizationsStepAfterStateId(),
        });

        this.listOrganizations();
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
            const afterStateId = this._getOrganizationsStepAfterStateId();

            this.renderStep({
                stateId: STATE.STEP_CHOOSE_ORGANIZATION,
                stepElement: <GithubOrgListStep />,
                afterStateId,
            });
        } else {
            this.renderStep({
                stateId: STATE.ERROR_UNKOWN,
                stepElement: <GithubUnknownErrorStep message={response.error} />,
            });
        }
    }

    @action
    selectOrganization(organization) {
        this.selectedOrganization = organization;

        this._creationApi.findExistingOrgFolder(this.selectedOrganization)
            .then(waitAtLeast(MIN_DELAY))
            .then(result => this._findExistingOrgFolderResult(result))
            .catch(error => console.log(error));

        this.renderStep({
            stateId: STATE.PENDING_LOADING_ORGANIZATIONS,
            stepElement: <GithubLoadingStep />,
            afterStateId: STATE.STEP_CHOOSE_ORGANIZATION,
        });
    }

    @action
    _findExistingOrgFolderResult(result) {
        const { isFound, isOrgFolder, orgFolder } = result;

        if (isFound && isOrgFolder) {
            LOGGER.debug(`selected existing org folder: ${orgFolder.name}`);
            this.existingOrgFolder = orgFolder;

            this.renderStep({
                stateId: STATE.STEP_CHOOSE_DISCOVER,
                stepElement: <GithubChooseDiscoverStep />,
                afterStateId: STATE.STEP_CHOOSE_ORGANIZATION,
            });
        } else if (!result.isFound) {
            LOGGER.debug('selected new organization');

            this.renderStep({
                stateId: STATE.STEP_CHOOSE_DISCOVER,
                stepElement: <GithubChooseDiscoverStep />,
                afterStateId: STATE.STEP_CHOOSE_ORGANIZATION,
            });
        } else {
            this.renderStep({
                stateId: STATE.STEP_INVALID_ORGFOLDER,
                stepElement: <GithubInvalidOrgFolderStep />,
                afterStateId: STATE.STEP_CHOOSE_ORGANIZATION,
            });
            this.setPlaceholders();
        }
    }

    @action
    selectDiscover(discover) {
        this.selectedAutoDiscover = discover;

        if (this.existingAutoDiscover && discover) {
            this.renderStep({
                stateId: STATE.STEP_ALREADY_DISCOVER,
                stepElement: <GithubAlreadyDiscoverStep />,
                afterStateId: STATE.STEP_CHOOSE_DISCOVER,
            });
        } else {
            this._loadAllRepositories(this.selectedOrganization);
            this.renderStep({
                stateId: STATE.PENDING_LOADING_REPOSITORIES,
                stepElement: <GithubLoadingStep />,
                afterStateId: STATE.STEP_CHOOSE_DISCOVER,
            });
        }
    }

    saveAutoDiscover() {
        this._saveOrgFolder();
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
        const cachedRepos = this._repositoryCache[organization.name];

        if (cachedRepos) {
            promise = new Promise(resolve => resolve({ repositories: { items: cachedRepos } }));
        } else {
            promise = this._loadPagedRepository(organization.name, FIRST_PAGE);
        }

        promise
            .then(waitAtLeast(MIN_DELAY))
            .then(repos => this._updateRepositories(organization.name, repos))
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

        if (this.selectedAutoDiscover && !morePages) {
            // wait until all the repos are loaded since we might
            // need the full list to display some data in confirm messages
            // TODO: might be able to optimize this to render after first page
            this.renderStep({
                stateId: STATE.STEP_CONFIRM_DISCOVER,
                stepElement: <GithubConfirmDiscoverStep />,
                afterStateId: STATE.STEP_CHOOSE_DISCOVER,
            });
        } else if (!this.selectedAutoDiscover && firstPage) {
            // render the repo list only once, after the first page comes back
            // otherwise we'll lose step's internal state
            this.renderStep({
                stateId: STATE.STEP_CHOOSE_REPOSITORY,
                stepElement: <GithubRepositoryStep />,
                afterStateId: STATE.STEP_CHOOSE_DISCOVER,
            });
        }
    }

    saveSingleRepo() {
        this._saveOrgFolder([this.selectedRepository.name]);
    }

    /**
     * Save the org folder with the specified list of repo names.
     * If omitted, the created org folder will scan all repos.
     *
     * @param repoNames
     * @private
     */
    @action
    _saveOrgFolder(repoNames = []) {
        const afterStateId = this.isStateAdded(STATE.STEP_CHOOSE_REPOSITORY) ?
            STATE.STEP_CHOOSE_REPOSITORY : STATE.STEP_CONFIRM_DISCOVER;

        this.renderStep({
            stateId: STATE.PENDING_CREATION_SAVING,
            stepElement: <GithubCompleteStep />,
            afterStateId,
        });

        this.setPlaceholders();

        this._initListeners();

        this._creationApi.createOrgFolder(this.credentialId, this.getScmId(), this.getApiUrl(), this.selectedOrganization, repoNames)
            .then(waitAtLeast(MIN_DELAY * 2))
            .then(r => this._saveOrgFolderSuccess(r), e => this._saveOrgFolderFailure(e));
    }

    @action
    _saveOrgFolderSuccess(orgFolder) {
        LOGGER.debug(`org folder saved successfully: ${orgFolder.name}`);
        this.changeState(STATE.PENDING_CREATION_EVENTS);
        this.savedOrgFolder = orgFolder;
    }

    _saveOrgFolderFailure() {
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
        // if the org folder hasn't been saved yet
        // or the event is not related to the org folder (or one of its children)
        // then we are not interested in this event: bail
        if (!this.savedOrgFolder || event.blueocean_job_rest_url.indexOf(this.savedOrgFolder._links.self.href) !== 0) {
            return;
        }

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

        if (this.selectedAutoDiscover && event.job_multibranch_indexing_result === 'SUCCESS') {
            this._incrementPipelineCount();
        }

        if (this.selectedAutoDiscover && event.job_orgfolder_indexing_result === 'SUCCESS') {
            // when the org folder indexing is complete, mbp indexing might still be running
            if (this.queuedIndexAllocations <= 0) {
                this._finishListening(STATE.STEP_COMPLETE_SUCCESS);
            }
        }

        if (this.selectedAutoDiscover && event.job_multibranch_indexing_result === 'SUCCESS') {
            this._checkForSingleRepoCreation(event.blueocean_job_pipeline_name, false, ({ isFound }) => {
                if (isFound || this.queuedIndexAllocations <= 0) {
                    this._finishListening(STATE.STEP_COMPLETE_SUCCESS);
                }
            });
        }

        if (!this.selectedAutoDiscover) {
            const pipelineFullName = `${this.selectedOrganization.name}/${this.selectedRepository.name}`;
            const orgIndexingComplete = event.job_orgfolder_indexing_result === 'SUCCESS';
            const multiBranchIndexingComplete = event.job_multibranch_indexing_result === 'SUCCESS' &&
                    event.blueocean_job_pipeline_name === pipelineFullName;

            if (orgIndexingComplete || multiBranchIndexingComplete) {
                this._checkForSingleRepoCreation(pipelineFullName, multiBranchIndexingComplete, ({ isFound, pipeline }) => {
                    if (isFound) {
                        this._completeSingleRepo(pipeline);
                    } else {
                        this._finishListening(STATE.STEP_COMPLETE_MISSING_JENKINSFILE);
                    }
                });
            }
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
        if (event.job_orgfolder_indexing_result === 'SUCCESS' || event.job_orgfolder_indexing_result === 'FAILURE') {
            LOGGER.debug(`indexing for ${event.blueocean_job_pipeline_name} finished: ${event.job_orgfolder_indexing_result}`);
        } else if (event.job_multibranch_indexing_result === 'SUCCESS' || event.job_multibranch_indexing_result === 'FAILURE') {
            LOGGER.debug(`indexing for ${event.blueocean_job_pipeline_name} finished: ${event.job_multibranch_indexing_result}`);
        } else if (event.jenkins_event === 'job_crud_created') {
            // LOGGER.debug(`created branch: ${event.job_name}`);
        }
    }

}
