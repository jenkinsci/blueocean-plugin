import React from 'react';
import ScmProvider from '../../ScmProvider';

import { BbCreationApi } from '../api/BbCreationApi';

import BbDefaultOption from '../BbDefaultOption';
import BbCloudFlowManager from './BbCloudFlowManager';

export default class BbCloudScmProvider extends ScmProvider {

    manager = null;

    getDefaultOption() {
        return <BbDefaultOption label="Bitbucket Cloud" />;
    }

    getFlowManager() {
        const creationApi = new BbCreationApi('bitbucket-cloud');
        this.manager = new BbCloudFlowManager(creationApi);
        return this.manager;
    }

    destroyFlowManager() {
        if (this.manager) {
            this.manager.destroy();
            this.manager = null;
        }
    }
}
