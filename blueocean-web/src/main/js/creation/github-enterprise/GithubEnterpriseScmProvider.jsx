import React from 'react';
import GithubScmProvider from '../github/GithubScmProvider';

import { BbCreationApi } from '../bitbucket/api/BbCreationApi';
import GHEServerApi from './api/GHEServerApi';

import GithubDefaultOption from '../github/GithubDefaultOption';
import GithubEnterpriseFlowManager from './GithubEnterpriseFlowManager';


export default class GithubEnterpriseScmProvider extends GithubScmProvider {

    manager = null;

    getDefaultOption() {
        return <GithubDefaultOption className="github-enterprise-creation" label="GitHub Enterprise" />;
    }

    getFlowManager() {
        const creationApi = new BbCreationApi('github-enterprise');
        const serverApi = new GHEServerApi();

        this.manager = new GithubEnterpriseFlowManager(creationApi, serverApi);
        return this.manager;
    }

    destroyFlowManager() {
        if (this.manager) {
            this.manager.destroy();
            this.manager = null;
        }
    }
}
