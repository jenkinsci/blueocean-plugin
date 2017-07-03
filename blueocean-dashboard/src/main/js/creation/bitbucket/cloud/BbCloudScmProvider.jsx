import React from 'react';
import ScmProvider from '../../ScmProvider';

import { BbCreationApi } from '../api/BBCreationApi';
import { BbCredentialsApi } from '../api/BbCredentialsApi';

import BbDefaultOption from '../BbDefaultOption';
import BbCloudFlowManager from './BbCloudFlowManager';

export default class BbCloudScmProvider extends ScmProvider {

    manager = null;

    getDefaultOption() {
        return <BbDefaultOption className="github-enterprise-creation" label="BitBucket Cloud" />;
    }

    getFlowManager() {
        const creationApi = new BbCreationApi('bitbucket-cloud');
        const credentialsApi = new BbCredentialsApi('bitbucket-cloud');

        this.manager = new BbCloudFlowManager(creationApi, credentialsApi);
        return this.manager;
    }

    destroyFlowManager() {
        if (this.manager) {
            this.manager.destroy();
            this.manager = null;
        }
    }
}
