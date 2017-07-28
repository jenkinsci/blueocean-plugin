import React from 'react';
import ScmProvider from '../ScmProvider';

import { GithubCreationApi } from './api/GithubCreationApi';

import GithubDefaultOption from './GithubDefaultOption';
import GithubFlowManager from './GithubFlowManager';

export default class GithubScmProvider extends ScmProvider {

    manager = null;

    getDefaultOption() {
        return <GithubDefaultOption />;
    }

    getFlowManager() {
        const creationApi = new GithubCreationApi();

        this.manager = new GithubFlowManager(creationApi);
        return this.manager;
    }

    destroyFlowManager() {
        if (this.manager) {
            this.manager.destroy();
            this.manager = null;
        }
    }
}
