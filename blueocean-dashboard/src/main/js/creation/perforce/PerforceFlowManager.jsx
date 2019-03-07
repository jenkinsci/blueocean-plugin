import React from 'react';
import { action, computed, observable } from 'mobx';
import Promise from 'bluebird';

import { i18nTranslator, logging, sseService, pipelineService } from '@jenkins-cd/blueocean-core-js';
const translate = i18nTranslator('blueocean-dashboard');

import FlowManager from '../CreationFlowManager';
import waitAtLeast from '../flow2/waitAtLeast';
import STATE from "../perforce/PerforceCreationState";
import PerforceLoadingStep from "./steps/PerforceLoadingStep";
import PerforceCredentialsStep from "./steps/PerforceCredentialStep";
import {ListOrganizationsOutcome} from "./api/PerforceCreationApi";
import PerforceOrgListStep from "./steps/PerforceOrgListStep";
import PerforceUnknownErrorStep from "./steps/PerforceUnknownErrorStep";
import GithubLoadingStep from "../github/steps/GithubLoadingStep";

const LOGGER = logging.logger('io.jenkins.blueocean.p4-pipeline');
const MIN_DELAY = 500;
const SAVE_DELAY = 1000;

/**
 * Impl of FlowManager for perforce creation flow.
 */
export default class PerforceFlowManager extends FlowManager {
    apiUrl = null;

    credentialId = null;

    credentialSelected = false;

    @observable organizations = [];

    @observable repositories = [];

    @observable repositoriesLoading = false;

    @computed
    get selectableRepositories() {
        if (!this.repositories) {
            return [];
        }
        return this.repositories;
    }

    @observable selectedOrganization = null;

    @observable selectedRepository = null;

    @computed
    get stepsDisabled() {
        return (
            this.stateId === STATE.PENDING_CREATION_SAVING ||
            this.stateId === STATE.STEP_COMPLETE_SAVING_ERROR ||
            this.stateId === STATE.PENDING_CREATION_EVENTS ||
            this.stateId === STATE.STEP_COMPLETE_EVENT_ERROR ||
            this.stateId === STATE.STEP_COMPLETE_EVENT_TIMEOUT ||
            this.stateId === STATE.STEP_COMPLETE_MISSING_JENKINSFILE ||
            this.stateId === STATE.STEP_COMPLETE_SUCCESS
        );
    }

    _repositoryCache = {};

    _creationApi = null;

    _sseSubscribeId = null;

    _sseTimeoutId = null;

    pipeline = null;
    pipelineName = null;

    constructor(creationApi) {
        super();
        this._creationApi = creationApi;
    }

    translate(key, opts) {
        return translate(key, opts);
    }

    getState() {
        return STATE;
    }

    getStates() {
        return STATE.values();
    }

    getInitialStep() {
        return {
            stateId: STATE.PENDING_LOADING_CREDS,
            //TODO Change this to perforce?
            stepElement: <GithubLoadingStep />,
        };
    }

    onInitialized() {
        this._renderCredentialsStep();
        this.setPlaceholders('Complete');
        console.log("PerforceFlowManager onInitialized complete");
    }

    destroy() {
        this._cleanupListeners();
    }

    getScmId() {
        return 'perforce';
    }

    getApiUrl() {
        // TODO For test. Change this.
        return 'https://api.git.com';
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
        console.log("PerforceFlowManager _renderCredentialsStep");
        this.renderStep({
            stateId: STATE.STEP_ACCESS_TOKEN,
            stepElement: (
                <PerforceCredentialsStep
                    scmId={this.getScmId()}
                    onCredentialSelected={(cred, selectionType) => this._onCredentialSelected(cred, selectionType)}
                />
            ),
            afterStateId: this._getCredentialsStepAfterStateId(),
        });
    }

    _onCredentialSelected(credential, selectionType) {
        console.log("PerforceFlowManager _onCredentialSelected");
        this.credentialId = credential.credentialId;
        this.credentialSelected = selectionType === 'userSelected';
        //this._renderLoadingOrganizations();
        console.log("PerforceFlowManager _onCredentialSelected end");
    }

    /**
     * stateId of the step after which the 'PerforceOrgListStep' should be added
     * @returns {string}
     * @private
     */
    _getOrganizationsStepAfterStateId() {
        // if the credential was manually selected, add the organizations step after it
        // if auto-selected, just replace it altogether
        return this.credentialSelected ? STATE.STEP_ACCESS_TOKEN : null;
    }

    _renderLoadingOrganizations() {
        this.renderStep({
            stateId: STATE.PENDING_LOADING_ORGANIZATIONS,
            stepElement: <PerforceLoadingStep />,
            afterStateId: this._getOrganizationsStepAfterStateId(),
        });

        this.listOrganizations();
    }

    @action
    listOrganizations() {
        this._creationApi
            .listOrganizations(this.credentialId, this.getApiUrl())
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
                stepElement: <PerforceOrgListStep />,
                afterStateId,
            });
        } else {
            this.renderStep({
                stateId: STATE.ERROR_UNKOWN,
                stepElement: <PerforceUnknownErrorStep message={response.error} />,
            });
        }
    }

    @action
    selectOrganization(organization) {
        this.selectedOrganization = organization;
        this.renderStep({
            stateId: STATE.PENDING_LOADING_ORGANIZATIONS,
            stepElement: <PerforceLoadingStep />,
            afterStateId: STATE.STEP_CHOOSE_ORGANIZATION,
        });
        this._loadAllRepositories(this.selectedOrganization);
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

    //TODO here

    createPipeline(repositoryUrl, credential) {
        console.log(repositoryUrl);
        console.log(credential);
        this.repositoryUrl = repositoryUrl;
        this.selectedCredential = credential;
        //TODO What is the need of this method? Find out then add
        //this.pipelineName = this._createNameFromRepoUrl(repositoryUrl);
        return this._initiateCreatePipeline();
    }

}
