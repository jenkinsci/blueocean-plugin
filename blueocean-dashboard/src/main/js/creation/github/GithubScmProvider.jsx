/**
 * Created by cmeyers on 10/17/16.
 */
import React from 'react';
import ScmProvider from '../ScmProvider';
import GithubDefaultOption from './GithubDefaultOption';
import GithubFlowManager from './GithubFlowManager';
import GithubCreationApi from './GithubCreationApi';

export default class GithubScmProvider extends ScmProvider {

    manager: null;

    constructor() {
        super();
        const api = new GithubCreationApi();
        this.manager = new GithubFlowManager(api);
    }

    getDefaultOption() {
        return <GithubDefaultOption />;
    }

    getFlowManager() {
        return this.manager;
    }
}
