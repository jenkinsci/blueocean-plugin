import React from 'react';
import GithubScmProvider from '../github/GithubScmProvider';

import { GithubCreationApi } from '../github/api/GithubCreationApi';
import { GithubCredentialsApi } from '../github/api/GithubCredentialsApi';

import GithubDefaultOption from '../github/GithubDefaultOption';
import GithubEnterpriseFlowManager from './GithubEnterpriseFlowManager';


export default class GithubEnterpriseScmProvider extends GithubScmProvider {

    manager = null;

    getDefaultOption() {
        return <GithubDefaultOption className="github-enterprise-creation" label="GitHub Enterprise" />;
    }

    getFlowManager() {
        const creationApi = new GithubCreationApi('github-enterprise');
        const credentialsApi = new GithubCredentialsApi('github-enterprise');

        this.manager = new GithubEnterpriseFlowManager(creationApi, credentialsApi);
        return this.manager;
    }

    destroyFlowManager() {
        if (this.manager) {
            this.manager.destroy();
            this.manager = null;
        }
    }
}
