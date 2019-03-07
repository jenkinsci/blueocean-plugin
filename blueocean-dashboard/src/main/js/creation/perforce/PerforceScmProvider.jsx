import React from 'react';
import ScmProvider from '../ScmProvider';
import PerforceDefaultOption from './PerforceDefaultOption';
import PerforceCreationApi from './api/PerforceCreationApi';

import PerforceFlowManager from './PerforceFlowManager';
import {BbCreationApi} from "../bitbucket/api/BbCreationApi";


/**
 * Provides the impl of FlowManager and the button for starting the Perforce flow.
 */
export default class PerforceScmProvider extends ScmProvider {
    getDefaultOption() {
        return <PerforceDefaultOption />;
    }

    getFlowManager() {
        const createApi = new PerforceCreationApi();
        //const createApi = new BbCreationApi('github');
        return new PerforceFlowManager(createApi);
    }

    destroyFlowManager() {}
}
