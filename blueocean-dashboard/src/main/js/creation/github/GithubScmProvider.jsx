import React from 'react';
import ScmProvider from '../ScmProvider';

import { GithubCreationApi } from './api/GithubCreationApi';
import { GithubCredentialsApi } from './api/GithubCredentialsApi';

import GithubDefaultOption from './GithubDefaultOption';
import GithubFlowManager from './GithubFlowManager';

export default class GithubScmProvider extends ScmProvider {

    manager = null;

    getDefaultOption() {
        return <GithubDefaultOption />;
    }

    getFlowManager() {
        const creationApi = new GithubCreationApi();
        const credentialsApi = new GithubCredentialsApi();

        this.manager = new GithubFlowManager(creationApi, credentialsApi);
        return this.manager;
    }

    destroyFlowManager() {
        if (this.manager) {
            this.manager.destroy();
            this.manager = null;
        }
    }
}
