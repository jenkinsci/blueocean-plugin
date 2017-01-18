import React from 'react';
import { action, observable } from 'mobx';

import pause from '../flow2/pause';
import FlowManager from '../flow2/FlowManager';
import GithubInitialStep from './steps/GithubInitialStep';
import GithubCredentialsStep from './steps/GithubCredentialStep';
import GithubOrgListStep from './steps/GithubOrgListStep';

export default class GithubFlowManager extends FlowManager {

    @observable
    organizations = [];

    @observable
    repositories = {};

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
            .then(pause)
            .then(credential => this._afterInitialStep(credential));
    }

    _afterInitialStep(credential) {
        console.log('cred:', credential);

        if (credential && credential.credentialId) {
            this.replaceCurrentStep(<GithubOrgListStep />);
            this.setPendingSteps([
                'Set Pending Step',
                'Another Pending Step',
            ]);
        } else {
            this.replaceCurrentStep(<GithubCredentialsStep />);
        }

        return null;
    }

    @action
    listOrganizations() {
        return this._api.listOrganizations()
            .then(orgs => { this._updateOrganizations(orgs); });
    }

    @action
    _updateOrganizations(organizations) {
        this.organizations = organizations;

        // TODO: temporary hack to toggle between authed / unauthed flow
        const showOrganizations = true;

        if (showOrganizations) {
            this.replaceCurrentStep(<GithubOrgListStep />);
            this.setPendingSteps([
                'Set Pending Step',
                'Another Pending Step',
            ]);
        } else {
            this.replaceCurrentStep(<GithubCredentialsStep />);
        }
    }

}
