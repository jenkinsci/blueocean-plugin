import React from 'react';
import GithubScmProvider from '../github/GithubScmProvider';

import { GithubCreationApi } from '../github/api/GithubCreationApi';
import { GithubCredentialsApi } from '../github/api/GithubCredentialsApi';
// import GHEServerApi from './api/GHEServerApi';
import GHEServerApi from './api/mock/GHEServerApiMock';

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
        const serverApi = new GHEServerApi();

        this.manager = new GithubEnterpriseFlowManager(creationApi, credentialsApi, serverApi);
        return this.manager;
    }

    destroyFlowManager() {
        if (this.manager) {
            this.manager.destroy();
            this.manager = null;
        }
    }
}
