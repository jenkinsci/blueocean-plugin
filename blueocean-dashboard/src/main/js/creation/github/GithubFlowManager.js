import React from 'react';
import { action, computed, observable } from 'mobx';

import waitAtLeast from '../flow2/waitAtLeast';

import FlowManager from '../flow2/FlowManager';
import GithubInitialStep from './steps/GithubInitialStep';
import GithubCredentialsStep from './steps/GithubCredentialStep';
import GithubOrgListStep from './steps/GithubOrgListStep';
import GithubChooseDiscoverStep from './steps/GithubChooseDiscoverStep';
import GithubConfirmDiscoverStep from './steps/GithubConfirmDiscoverStep';
import GithubRepositoryStep from './steps/GithubRepositoryStep';

export default class GithubFlowManager extends FlowManager {

    @observable
    organizations = [];

    @observable
    repositories = {};

    @computed
    get repos() {
        return this.repositories[this._selectedOrganization.name];
    }

    _selectedOrganization = null;

    _discoverSelection = null;

    _selectedRepository = null;

    _credentialId = null;

    _creationApi = null;

    _credentialsApi = null;

    constructor(creationApi, credentialsApi) {
        super();

        this._creationApi = creationApi;
        this._credentialsApi = credentialsApi;
    }

    getInitialStep() {
        return <GithubInitialStep />;
    }

    findExistingCredential() {
        return this._credentialsApi.findExistingCredential()
            .then(waitAtLeast(1000))
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

    @action
    listOrganizations() {
        return this._creationApi.listOrganizations(this._credentialId)
            .then(waitAtLeast(1000))
            .then(orgs => { this._updateOrganizations(orgs); });
    }

    @action
    _updateOrganizations(organizations) {
        this.organizations = organizations;

        this.replaceCurrentStep(<GithubOrgListStep />);
        this.setPendingSteps([
            'Set Pending Step',
            'Another Pending Step',
        ]);
    }

    @action
    selectOrganization(organization) {
        this._selectedOrganization = organization;
        this.pushStep(<GithubChooseDiscoverStep />);
    }

    selectDiscover(discover) {
        this._discoverSelection = discover;

        if (!discover) {
            this._loadAllRepositories(this._selectedOrganization);
        } else {
            this.pushStep(<GithubConfirmDiscoverStep />);
        }
    }

    _loadAllRepositories(organization) {
        this._creationApi.listRepositories(this._credentialId, organization.name, 0, 100)
            .then(repos => this._updateRepositories(organization.name, repos));
    }

    @action
    _updateRepositories(organizationName, repos) {
        this.repositories[organizationName] = repos;
        this.pushStep(<GithubRepositoryStep />);
    }

}
