import React from 'react';
import ScmProvider from '../ScmProvider';

// import { GithubCreationApi } from './api/GithubCreationApi';
// import { GithubCredentialsApi } from './api/GithubCredentialsApi';

import { GithubCreationApi } from './api/mocks/GithubCreationApiMock';
import { GithubCredentialsApi } from './api/mocks/GithubCredentialsApiMock';

import GithubDefaultOption from './GithubDefaultOption';
import GithubFlowManager from './GithubFlowManager';

export default class GithubScmProvider extends ScmProvider {

    manager = null;
    _creationApi = null;
    _credentialsApi = null;

    constructor() {
        super();

        this._creationApi = new GithubCreationApi();
        this._credentialsApi = new GithubCredentialsApi();

        this.manager = new GithubFlowManager(
            this._creationApi, this._credentialsApi
        );
    }

    getDefaultOption() {
        return <GithubDefaultOption />;
    }

    getFlowManager() {
        return this.manager;
    }

    destroyFlowManager() {
        if (this.manager) {
            this.manager.destroy();
        }
    }
}
