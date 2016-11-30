/**
 * Created by cmeyers on 11/30/16.
 */
import React from 'react';

import FlowManager from '../flow2/FlowManager';
import GithubInitialStep from './steps/GithubInitialStep';

export default class GithubFlowManager extends FlowManager {

    organizations: [];
    repositories: {};

    constructor(api) {
        super();

        this._api = api;
    }

    listOrganizations() {
        return this._api.listOrganizations()
            .then(orgs => { this.organizations = orgs; });
    }

    onInitialize() {
        console.log('ghfm onInit');
    }

    getInitialStep() {
        return <GithubInitialStep />;
    }

}
