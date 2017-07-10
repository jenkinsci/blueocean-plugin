import React from 'react';
import ScmProvider from '../../ScmProvider';

import { BbCreationApi } from '../api/BbCreationApi';
import { BbCredentialsApi } from '../api/BbCredentialsApi';

import BbDefaultOption from '../BbDefaultOption';
import BbServerFlowManager from './BbServerFlowManager';
import BbServerApi from './api/BbServerApi';

export default class BbServerScmProvider extends ScmProvider {

    manager = null;

    getDefaultOption() {
        return <BbDefaultOption className="github-enterprise-creation" label="Bitbucket Server" />;
    }

    getFlowManager() {
        const creationApi = new BbCreationApi('bitbucket-server');
        const credentialsApi = new BbCredentialsApi('bitbucket-server');
        const serverApi = new BbServerApi();
        this.manager = new BbServerFlowManager(creationApi, credentialsApi, serverApi);
        return this.manager;
    }

    destroyFlowManager() {
        if (this.manager) {
            this.manager.destroy();
            this.manager = null;
        }
    }
}
