/**
 * Created by cmeyers on 11/30/16.
 */
import React from 'react';
import { action, observable } from 'mobx';

import FlowManager from '../flow2/FlowManager';
import GithubInitialStep from './steps/GithubInitialStep';
import GithubCredentialsStep from './steps/GithubCredentialStep';
import GithubOrgListStep from './steps/GithubOrgListStep';

export default class GithubFlowManager extends FlowManager {

    @observable
    organizations = [];

    @observable
    repositories = {};

    constructor(api) {
        super();

        this._api = api;
    }

    getInitialStep() {
        return <GithubInitialStep />;
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
