import React from 'react';
import { assert } from 'chai';
import configureMockStore from 'redux-mock-store';
import thunk from 'redux-thunk';
import nock from 'nock';

import { getNodesInformation } from './../../main/js/util/logDisplayHelper';
import {
    actions,
} from '../../main/js/redux';
import { nodes, stepsNode45 } from './nodes';

import {
    calculateRunLogURLObject, calculateStepsBaseUrl, calculateNodeBaseUrl,
} from '../../main/js/util/UrlUtils';

import { TestUtils } from '@jenkins-cd/blueocean-core-js';

const middlewares = [thunk];
const mockStore = configureMockStore(middlewares);

describe("Store should work", () => {
    describe("Log Store should work", () => {
        afterEach(() => {
            nock.cleanAll()
        });
        it("create store with run data", () => {
            const name = 'testName',
                runId = 4,
                branch = 'testing',
                _appURLBase = '',
                isMultiBranch = false;
            const mergedConfig = {name, runId, branch, _appURLBase, isMultiBranch};
            const logGeneral = calculateRunLogURLObject(mergedConfig);
            nock('http://example.com')
                .get(logGeneral.url)
                .reply(200, `Hello World 2 parallel
[workspace] Running shell script
+ date
Tue May 24 13:42:18 CEST 2016
+ sleep 20
+ date
Tue May 24 13:42:38 CEST 2016
`);
            const store = mockStore({adminStore: {logs: {}}});
            logGeneral.url = `http://example.com${logGeneral.url}`;
            TestUtils.patchFetchNoJWT();
            return store.dispatch(
                actions.fetchLog({...logGeneral}))
                .then(() => { // return of async actions
                    assert.equal(store.getActions()[0].type, 'SET_LOGS');
                    assert.equal(store.getActions()[0].payload.logUrl, logGeneral.url);
                });
        });
    });

    describe("Store should work with steps", () => {
        afterEach(() => {
            nock.cleanAll()
        });

        it("create store with step data", () => {
            const baseUrl = 'http://127.0.0.1';
            const name = 'steps',
                runId = 16,
                branch = 'testing',
                _appURLBase = '',
                isMultiBranch = false;
            const mergedConfig = {name, runId, branch, _appURLBase, isMultiBranch};
            const node = 45;
            const stepsUrl = calculateStepsBaseUrl({...mergedConfig, node});
            const stepsNock = nock(baseUrl)
                .get(stepsUrl)
                .reply(200, stepsNode45)
            ;
            const store = mockStore({adminStore: {}});
            mergedConfig._appURLBase = `${baseUrl}`;
            TestUtils.patchFetchNoJWT();

            return store.dispatch(
                actions.fetchSteps({...mergedConfig, node}))
                .then(() => { // return of async actions
                    assert.equal(store.getActions()[0].type, 'SET_STEPS');
                    assert.equal(store.getActions()[0].payload.isFinished, true);
                    assert.equal(store.getActions()[0].payload.isError, false);
                    assert.equal(store.getActions()[0].payload.nodesBaseUrl, `${baseUrl}${stepsUrl}`);
                    assert.equal(store.getActions()[0].payload.model.length, 10);
                    assert.equal(stepsNock.isDone(), true);         
            });
    
        });
    });

    describe("Store should work with nodes", () => {
        afterEach(() => {
            nock.cleanAll()
        });

        it("create store with node data", () => {
            const baseUrl = 'http://127.0.0.1';
            const name = 'steps',
                runId = 16,
                branch = 'testing',
                _appURLBase = '',
                isMultiBranch = false;
            const mergedConfig = {name, runId, branch, _appURLBase, isMultiBranch};
            const nodesBaseUrl = calculateNodeBaseUrl(mergedConfig);
            const node = 45;
            const stepsUrl = calculateStepsBaseUrl({...mergedConfig, node});
            const nodeNock = nock(baseUrl)
                    .get(nodesBaseUrl)
                    .reply(200, nodes)
                ;
            mergedConfig._appURLBase = `${baseUrl}:80`;
            const steps = {};
            steps[`${baseUrl}:80${stepsUrl}`] = getNodesInformation(stepsNode45);
            const otherStore = mockStore({adminStore: {steps}});
            TestUtils.patchFetchNoJWT();

            otherStore.dispatch(actions.fetchNodes(mergedConfig));
            assert.equal(nodeNock.isDone(), true);
        });
    });

});
