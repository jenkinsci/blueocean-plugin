import React from 'react';
import ScmProvider from '../../ScmProvider';

import { BbCreationApi } from '../api/BbCreationApi';
import { BbCredentialsApi } from '../api/BbCredentialsApi';

import BbDefaultOption from '../BbDefaultOption';
import BbServerFlowManager from './BbServerFlowManager';

export default class BbServerScmProvider extends ScmProvider {

    manager = null;

    getDefaultOption() {
        return <BbDefaultOption className="github-enterprise-creation" label="BitBucket Server" />;
    }

    getFlowManager() {
        const creationApi = new BbCreationApi('bitbucket-server');
        const credentialsApi = new BbCredentialsApi('bitbucket-server');

        this.manager = new BbServerFlowManager(creationApi, credentialsApi);
        return this.manager;
    }

    destroyFlowManager() {
        if (this.manager) {
            this.manager.destroy();
            this.manager = null;
        }
    }
}
