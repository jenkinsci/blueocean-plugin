import React from 'react';
import { i18nTranslator } from '@jenkins-cd/blueocean-core-js';

import waitAtLeast from '../flow2/waitAtLeast';
import GithubFlowManager from '../github/GithubFlowManager';
import GithubLoadingStep from '../github/steps/GithubLoadingStep';

import GHEChooseServerStep from './steps/GHEChooseServerStep';
import GHEServerManager from './GHEServerManager';
import STATE from './GHECreationState';


const translate = i18nTranslator('blueocean-dashboard');
const MIN_DELAY = 500;


export default class GithubEnterpriseFlowManager extends GithubFlowManager {

    constructor(creationApi, credentialsApi, serverApi) {
        super(creationApi, credentialsApi);

        this.serverManager = new GHEServerManager(serverApi);
    }

    translate(key, opts) {
        return translate(key, opts);
    }

    getStates() {
        return STATE.values();
    }

    getInitialStep() {
        return {
            stateId: STATE.PENDING_LOADING_SERVERS,
            stepElement: <GithubLoadingStep />,
        };
    }

    onInitialized() {
        this._loadServerList();
        this.setPlaceholders('Complete');
    }

    _loadServerList() {
        return this.serverManager.listServers()
            .then(waitAtLeast(MIN_DELAY))
            .then(success => this._loadServerListComplete(success));
    }

    _loadServerListComplete() {
        this.renderStep({
            stateId: STATE.STEP_CHOOSE_SERVER,
            stepElement: <GHEChooseServerStep />,
        });
    }

}
