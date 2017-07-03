import React from 'react';
import { assert} from 'chai';
import configureMockStore from 'redux-mock-store';
import thunk from 'redux-thunk';
import nock from 'nock';
import mockFetch from './util/smart-fetch-mock';

const debugLog = require('debug')('store-spec:debug');

import {
    actions,
    ACTION_TYPES,
    allPipelines as pipelinesSelector,
    currentRuns as currentRunsSelector,
} from '../../main/js/redux';
import { pipelines } from './data/pipelines/pipelinesSingle';
import { latestRuns } from './data/runs/latestRuns';
import job_crud_created_multibranch from './data/sse/job_crud_created_multibranch';
import fetchedBranches from './data/branches/latestBranches';

import { Fetch, TestUtils } from '@jenkins-cd/blueocean-core-js';

const middlewares = [thunk];
const mockStore = configureMockStore(middlewares);

const actionsFetch = Fetch.fetchJSON;

xdescribe("Redux Store - ", () => {
    afterEach(() => {
        nock.cleanAll();
        Fetch.fetchJSON = actionsFetch;
    });

    /* TODO: Fix this test
    it("create store with pipeline data", () => {
        var ruleId = '/rest/organizations/jenkins/pipelines/';
        nock('http://example.com')
            .get(ruleId)
            .reply(200, pipelines);
        const store = mockStore({ adminStore: {allPipelines: [] }});

        return store.dispatch(
            actions.generateData('http://example.com' + ruleId, ACTION_TYPES.SET_ALL_PIPELINES_DATA))
            .then(() => { // return of async actions
                assert.equal(store.getActions()[0].type, 'SET_ALL_PIPELINES_DATA');
                assert.equal(store.getActions()[0].payload.length, pipelines.length);
                assert.equal(pipelinesSelector({adminStore: {allPipelines: pipelines}}).length, pipelines.length);
            });
    });*/

    it("create store with branch data", () => {
        var ruleId = '/rest/organizations/jenkins/pipelines/xxx/runs/';
        var baseUrl = 'http://example.com';
        nock(baseUrl)
            .get(ruleId)
            .reply(200, latestRuns);

        mockFetch(
            '/jenkins/blue/rest/organizations/orgg/pipelines/xxx/activities/',
            latestRuns);

        const store = mockStore({ adminStore: {}});
        return store.dispatch(
            actions.fetchRuns({
                getAppURLBase() {
                    return baseUrl;
                },
                organization: 'orgg',
                pipeline: 'xxx'
            }))
            .then(data => { // return of async actions
                assert.equal(currentRunsSelector({adminStore: {currentRuns: latestRuns}}).length, latestRuns.length);
                assert.equal(store.getActions()[0].type, 'SET_RUNS_DATA');
            });
    });

    const multi_branch_job_crud_job_created = (job_crud_sse_event) => {
        // Mock the fetching of the latest branches from the REST API.
        if (job_crud_sse_event.blueocean_is_for_current_job) {
            mockFetch(
                '/jenkins/blue/rest/organizations/jenkins/pipelines/tfprdemo/branches/?filter=origin',
                fetchedBranches);
        }

        const actionFunc = actions.updateBranchList(job_crud_sse_event, {
            getAppURLBase() {
                return 'http://example.com';
            }
        });

        const dispatches = [];
        const ret = actionFunc((dispatchConfig) => {
            dispatches.push(dispatchConfig);
        }, () => {
            // fetchedBranches is a 3 branch array. First 2 branches
            // are older branches (what's in the store) and the 3rd branch is
            // the new branch relating to the sse event.
            const currentStoreBranches = [
                fetchedBranches[0],
                fetchedBranches[1]
            ];
            return {
                adminStore: {
                    branches: {
                        tfprdemo: currentStoreBranches
                    }
                }
            };
        });
        return dispatches;
    };

    it("multi-branch job_crud_job_created blueocean_is_for_current_job=true", () => {
        const dispatches = multi_branch_job_crud_job_created(job_crud_created_multibranch);
        debugLog('dispatches: ', dispatches);

        // Should be 1 events dispatched because
        // blueocean_is_for_current_job=true
        assert.equal(dispatches.length, 1);
        assert.equal(dispatches[0].id, 'tfprdemo');
        assert.equal(dispatches[0].type, 'SET_CURRENT_BRANCHES_DATA');
        assert.equal(dispatches[0].payload.length, 3); // 3 branches as returned by the fetch
    });

    it("multi-branch job_crud_job_created blueocean_is_for_current_job=false", () => {
        // Copy the job_crud_created_multibranch event object and modify the
        // blueocean_is_for_current_job property to false. This switch off
        // one of the event dispatches.
        const sse_event = Object.assign({}, job_crud_created_multibranch);
        sse_event.blueocean_is_for_current_job = false;

        const dispatches = multi_branch_job_crud_job_created(sse_event);

        // Should only be 0 event dispatched because
        // blueocean_is_for_current_job=false
        assert.equal(dispatches.length, 0);
    });
});


