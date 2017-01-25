import React from 'react';
import { action, computed, observable } from 'mobx';

import waitAtLeast from '../flow2/waitAtLeast';

import FlowManager from '../flow2/FlowManager';
import STATUS from './GithubCreationStatus';
import GithubAlreadyDiscoverStep from './steps/GithubAlreadyDiscoverStep';
import GithubLoadingStep from './steps/GithubLoadingStep';
import GithubCredentialsStep from './steps/GithubCredentialStep';
import GithubOrgListStep from './steps/GithubOrgListStep';
import GithubChooseDiscoverStep from './steps/GithubChooseDiscoverStep';
import GithubConfirmDiscoverStep from './steps/GithubConfirmDiscoverStep';
import GithubRepositoryStep from './steps/GithubRepositoryStep';
import GithubCompleteStep from './steps/GithubCompleteStep';

const MIN_DELAY = 500;
const FIRST_PAGE = 1;
const PAGE_SIZE = 100;

export default class GithubFlowManager extends FlowManager {

    @observable
    status = null;

    @observable
    organizations = [];

    @observable
    repositories = [];

    @computed get selectableRepositories() {
        return this.repositories ? this.repositories.filter(repo => !repo.pipelineCreated) : [];
    }

    @observable
    selectedOrganization = null;

    @observable
    selectedRepository = null;

    _repositoryCache = {};

    _discoverSelection = null;

    _credentialId = null;

    _creationApi = null;

    _credentialsApi = null;

    constructor(creationApi, credentialsApi) {
        super();

        this._creationApi = creationApi;
        this._credentialsApi = credentialsApi;
    }

    getInitialStep() {
        return <GithubLoadingStep />;
    }

    onInitialized() {
        this.findExistingCredential();
    }

    findExistingCredential() {
        return this._credentialsApi.findExistingCredential()
            .then(waitAtLeast(MIN_DELAY))
            .then(credential => this._afterInitialStep(credential));
    }

    _afterInitialStep(credential) {
        if (credential && credential.credentialId) {
            this._credentialId = credential.credentialId;
            this.listOrganizations();
        } else {
            this.replaceCurrentStep(<GithubCredentialsStep />);
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

        this.pushStep(<GithubLoadingStep />);
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

        this.replaceCurrentStep(<GithubOrgListStep />);
        this.setPendingSteps([
            'Complete',
        ]);
    }

    @action
    selectOrganization(organization) {
        this.selectedOrganization = organization;
        this._setStatus(STATUS.STEP_CHOOSE_DISCOVER);
        this.pushStep(<GithubChooseDiscoverStep />);
    }

    selectDiscover(discover) {
        this._discoverSelection = discover;

        if (this.selectedOrganization.autoDiscover && discover) {
            this._setStatus(STATUS.STEP_ALREADY_DISCOVER);
            this.pushStep(<GithubAlreadyDiscoverStep />);
        } else {
            this._loadAllRepositories(this.selectedOrganization);
            this.pushStep(<GithubLoadingStep />);
        }
    }

    confirmDiscover() {
        if (!this.selectedOrganization.jenkinsOrganizationPipeline) {
            this._creationApi.createOrgFolder(this._credentialId, this.selectedOrganization);
        } else {
            // TODO: handle update case
            console.log('TODO: handle update case');
        }
    }

    @action
    selectRepository(repo) {
        this.selectedRepository = repo;
    }

    @action
    _loadAllRepositories(organization) {
        this.repositories.replace([]);

        this._loadPagedRepository(organization.name, FIRST_PAGE)
            .then(waitAtLeast(MIN_DELAY))
            .then(repos => this._updateRepositories(organization.name, repos, FIRST_PAGE));

        this._setStatus(STATUS.PENDING_LOADING_REPOSITORIES);
    }

    _loadPagedRepository(organizationName, pageNumber, pageSize = PAGE_SIZE) {
        return this._creationApi.listRepositories(this._credentialId, organizationName, pageNumber, pageSize);
    }

    @action
    _setStatus(status) {
        this.status = status;
    }

    @action
    _updateRepositories(organizationName, repoData) {
        const { items, nextPage } = repoData.repositories;

        this.repositories.push(...items);
        this._repositoryCache[organizationName] = this.repositories.slice();

        // if another page is available, keep fetching
        if (nextPage !== null) {
            this._loadPagedRepository(organizationName, nextPage)
                .then(repos2 => this._updateRepositories(organizationName, repos2, nextPage));
        } else {
            if (this._discoverSelection) {
                this.replaceCurrentStep(<GithubConfirmDiscoverStep />);
                this._setStatus(STATUS.STEP_CONFIRM_DISCOVER);
            } else {
                this.replaceCurrentStep(<GithubRepositoryStep />);
                this._setStatus(STATUS.STEP_CHOOSE_REPOSITORY);
            }
        }
    }

    createFromRepository() {
        this.pushStep(<GithubCompleteStep />);
        this.setPendingSteps();
    }

}
