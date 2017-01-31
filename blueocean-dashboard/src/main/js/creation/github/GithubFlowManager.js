import React from 'react';
import { action, computed, observable } from 'mobx';
import { sseService } from '@jenkins-cd/blueocean-core-js';

import waitAtLeast from '../flow2/waitAtLeast';

import FlowManager from '../flow2/FlowManager';
import STATE from './GithubCreationState';
import GithubAlreadyDiscoverStep from './steps/GithubAlreadyDiscoverStep';
import GithubLoadingStep from './steps/GithubLoadingStep';
import GithubCredentialsStep from './steps/GithubCredentialStep';
import GithubInvalidOrgFolderStep from './steps/GithubInvalidOrgFolderStep';
import GithubOrgListStep from './steps/GithubOrgListStep';
import GithubChooseDiscoverStep from './steps/GithubChooseDiscoverStep';
import GithubConfirmDiscoverStep from './steps/GithubConfirmDiscoverStep';
import GithubRepositoryStep from './steps/GithubRepositoryStep';
import GithubCompleteStep from './steps/GithubCompleteStep';

const MIN_DELAY = 500;
const FIRST_PAGE = 1;
const PAGE_SIZE = 100;
const SSE_TIMEOUT_DELAY = 1000 * 30;

export default class GithubFlowManager extends FlowManager {

    @observable
    organizations = [];

    @observable
    repositories = [];

    @computed get selectableRepositories() {
        // return repositories that are not already created as pipelines
        if (this.repositories && this.existingOrgFolder) {
            return this.repositories.filter(repo => (
                this.existingOrgFolder.pipelines.indexOf(repo.name) === -1
            ));
        }

        return [];
    }

    @observable
    selectedOrganization = null;

    @observable
    existingOrgFolder = null;

    @computed get existingAutoDiscover() {
        return this.existingOrgFolder && this.existingOrgFolder.requestedRepos && this.existingOrgFolder.requestedRepos.length === 0;
    }

    @observable
    selectedRepository = null;

    @observable
    savedOrgFolder = null;

    _repositoryCache = {};

    _discoverSelection = null;

    _credentialId = null;

    _creationApi = null;

    _credentialsApi = null;

    _sseSubscribeId = null;

    _sseTimeoutId = null;

    constructor(creationApi, credentialsApi) {
        super();

        this._creationApi = creationApi;
        this._credentialsApi = credentialsApi;
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
        this.findExistingCredential();
        this.setPlaceholders('Complete');
    }

    destroy() {
        this._cleanupListeners();
    }

    _cleanupListeners() {
        if (this._sseSubscribeId) {
            sseService.removeHandler(this._sseSubscribeId);
            this._sseSubscribeId = null;
        }
        if (this._sseTimeoutId) {
            clearTimeout(this._sseTimeoutId);
            this._sseTimeoutId = null;
        }
    }

    findExistingCredential() {
        return this._credentialsApi.findExistingCredential()
            .then(waitAtLeast(MIN_DELAY))
            .then(credential => this._afterInitialStep(credential));
    }

    _afterInitialStep(credential) {
        if (credential && credential.credentialId) {
            this._credentialId = credential.credentialId;
            this.changeState(STATE.PENDING_LOADING_ORGANIZATIONS);
            this.listOrganizations();
        } else {
            this.renderStep({
                stateId: STATE.STEP_ACCESS_TOKEN,
                stepElement: <GithubCredentialsStep />,
            });
        }
    }

    createAccessToken(token) {
        return this._credentialsApi.createAccessToken(token)
            .then(waitAtLeast(MIN_DELAY))
            .then(
                cred => this._createTokenSuccess(cred),
                error => this._createTokenFailure(error),
            );
    }

    _createTokenSuccess(cred) {
        this._credentialId = cred.credentialId;

        this.renderStep({
            stateId: STATE.PENDING_LOADING_ORGANIZATIONS,
            stepElement: <GithubLoadingStep />,
            afterStateId: STATE.STEP_ACCESS_TOKEN,
        });

        this.listOrganizations();

        return {
            success: true,
        };
    }

    _createTokenFailure(error) {
        return {
            success: false,
            detail: error.responseBody,
        };
    }

    @action
    listOrganizations() {
        return this._creationApi.listOrganizations(this._credentialId)
            .then(waitAtLeast(MIN_DELAY))
            .then(orgs => { this._updateOrganizations(orgs); });
    }

    @action
    _updateOrganizations(organizations) {
        this.organizations = organizations;

        const afterStateId = this.isStateAdded(STATE.STEP_ACCESS_TOKEN) ?
            STATE.STEP_ACCESS_TOKEN : null;

        this.renderStep({
            stateId: STATE.STEP_CHOOSE_ORGANIZATION,
            stepElement: <GithubOrgListStep />,
            afterStateId,
        });
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
            this.existingOrgFolder = orgFolder;

            this.renderStep({
                stateId: STATE.STEP_CHOOSE_DISCOVER,
                stepElement: <GithubChooseDiscoverStep />,
                afterStateId: STATE.STEP_CHOOSE_ORGANIZATION,
            });
        } else if (!result.isFound) {
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

    selectDiscover(discover) {
        this._discoverSelection = discover;

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

        let promise = null;
        const cachedRepos = this._repositoryCache[organization.name];

        if (cachedRepos) {
            promise = new Promise(resolve => resolve({ repositories: { items: cachedRepos } }));
        } else {
            promise = this._loadPagedRepository(organization.name, FIRST_PAGE);
        }

        promise
            .then(waitAtLeast(MIN_DELAY))
            .then(repos => this._updateRepositories(organization.name, repos, FIRST_PAGE))
            .catch(error => console.log(error));
    }

    _loadPagedRepository(organizationName, pageNumber, pageSize = PAGE_SIZE) {
        return this._creationApi.listRepositories(this._credentialId, organizationName, pageNumber, pageSize);
    }

    @action
    _updateRepositories(organizationName, repoData) {
        const { items, nextPage } = repoData.repositories;

        this.repositories.push(...items);
        this._repositoryCache[organizationName] = this.repositories.slice();

        const morePages = !isNaN(parseInt(nextPage, 10));

        if (morePages) {
            this._loadPagedRepository(organizationName, nextPage)
                .then(repos2 => this._updateRepositories(organizationName, repos2, nextPage));
        } else {
            if (this._discoverSelection) {
                this.renderStep({
                    stateId: STATE.STEP_CONFIRM_DISCOVER,
                    stepElement: <GithubConfirmDiscoverStep />,
                    afterStateId: STATE.STEP_CHOOSE_DISCOVER,
                });
            } else {
                this.renderStep({
                    stateId: STATE.STEP_CHOOSE_REPOSITORY,
                    stepElement: <GithubRepositoryStep />,
                    afterStateId: STATE.STEP_CHOOSE_DISCOVER,
                });
            }
        }
    }

    saveSingleRepo() {
        const repoNames = this._getFullRepoNameList();
        this._saveOrgFolder(repoNames);
    }

    /**
     * Get the full list of repo names for the org folder based on those already being scanned, and the user's selection.
     *
     * @returns {Array}
     * @private
     */
    _getFullRepoNameList() {
        const existingPipelines = this.existingOrgFolder && this.existingOrgFolder.requestedRepos.slice();
        return [].concat(existingPipelines, this.selectedRepository.name);
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

        const promise = !this.existingOrgFolder ?
            this._creationApi.createOrgFolder(this._credentialId, this.selectedOrganization, repoNames) :
            this._creationApi.updateOrgFolder(this._credentialId, this.existingOrgFolder, repoNames);

        promise
            .then(waitAtLeast(MIN_DELAY * 2))
            .then(r => this._saveOrgFolderSuccess(r), e => this._saveOrgFolderFailure(e));
    }

    @action
    _saveOrgFolderSuccess(orgFolder) {
        this.changeState(STATE.PENDING_CREATION_EVENTS);
        this.savedOrgFolder = orgFolder;
        this._sseSubscribeId = sseService.registerHandler(event => this._onSseEvent(event));
        this._sseTimeoutId = setTimeout(() => {
            this._onSseTimeout();
        }, SSE_TIMEOUT_DELAY);
    }

    _saveOrgFolderFailure() {
        this.changeState(STATE.STEP_COMPLETE_SAVING_ERROR);
    }

    _onSseEvent(event) {
        if (event.blueocean_job_rest_url.indexOf(this.savedOrgFolder._links.self.href) === 0) {
            if (event.jenkins_event === 'job_run_queue_task_complete') {
                // TODO: investigate why in some cases we seem to receive this event but without 'job_multibranch_indexing' props
                // these fields might not be populated in the event of RateLimitExceededException
                if (event.job_multibranch_indexing_result === 'SUCCESS') {
                    this.changeState(STATE.STEP_COMPLETE_SUCCESS);
                    this._cleanupListeners();
                } else if (event.job_multibranch_indexing_result === 'FAILURE') {
                    this.changeState(STATE.STEP_COMPLETE_EVENT_ERROR);
                    this._cleanupListeners();
                }
            }
        }
    }

    _onSseTimeout() {
        this.changeState(STATE.STEP_COMPLETE_EVENT_TIMEOUT);
        this._cleanupListeners();
    }

}
