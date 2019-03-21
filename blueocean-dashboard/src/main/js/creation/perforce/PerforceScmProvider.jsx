import React from 'react';
import ScmProvider from '../ScmProvider';
import PerforceDefaultOption from './PerforceDefaultOption';
import PerforceCreationApi from '../perforce/api/PerforceCreationApi';

import PerforceFlowManager from './PerforceFlowManager';
import PerforceCredentialsApi from "../../credentials/perforce/PerforceCredentialsApi";


/**
 * Provides the impl of FlowManager and the button for starting the Perforce flow.
 */
export default class PerforceScmProvider extends ScmProvider {
    getDefaultOption() {
        return <PerforceDefaultOption />;
    }

    getFlowManager() {
        //TODO Remove perforce hardcoding
        const createApi = new PerforceCreationApi('perforce');
        const credentialApi = new PerforceCredentialsApi('perforce');
        return new PerforceFlowManager(createApi, credentialApi);
    }

    destroyFlowManager() {}
}
