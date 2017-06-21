import React from 'react';

import STATE from '../github/GithubCreationState';
import GithubFlowManager from '../github/GithubFlowManager';
import GithubCredentialStep from '../github/steps/GithubCredentialStep';


export default class GithubEnterpriseFlowManager extends GithubFlowManager {

    onInitialized() {
        this._renderCredentialsStep();
        this.setPlaceholders('Complete');
    }

    _renderCredentialsStep() {
        // TOOD: we may just end up creating a new step entirely since there are so many differences
        this.renderStep({
            stateId: STATE.STEP_ACCESS_TOKEN,
            stepElement: <GithubCredentialStep enterpriseMode />,
        });
    }

    _findExistingCredentialComplete(success) {
        if (success) {
            this._renderLoadingOrganizations();
        }
    }

}
