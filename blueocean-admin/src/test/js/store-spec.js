import React from 'react';
import { assert} from 'chai';
import { shallow } from 'enzyme';
import configureMockStore from 'redux-mock-store';
import thunk from 'redux-thunk';
import nock from 'nock';

import {
    actions,
    ACTION_TYPES,
    pipelines as pipelinesSelector,
    currentRuns as currentRunsSelector,
} from '../../main/js/redux';
import { pipelines } from './pipelines';
import { latestRuns } from './latestRuns';

const middlewares = [thunk];
const mockStore = configureMockStore(middlewares);

describe("Store should work", () => {
    afterEach(() => {
        nock.cleanAll()
    })
    it("create store with pipeline data", () => {
        var ruleId = '/rest/organizations/jenkins/pipelines/';
        nock('http://example.com/')
            .get(ruleId)
            .reply(200, pipelines);
        const store = mockStore({ adminStore: {pipelines: [] }});

        return store.dispatch(
            actions.generateData('http://example.com' + ruleId, ACTION_TYPES.SET_PIPELINES_DATA))
            .then(() => { // return of async actions
                assert.equal(store.getActions()[0].type, 'SET_PIPELINES_DATA');
                assert.equal(store.getActions()[0].payload.length, pipelines.length);
                assert.equal(pipelinesSelector({adminStore: {pipelines}}).length, pipelines.length);
            });
    });
    it("create store with branch data", () => {
        var ruleId = '/rest/organizations/jenkins/pipelines/xxx/runs';
        var baseUrl = 'http://example.com/';
        nock(baseUrl)
            .get(ruleId)
            .reply(200, latestRuns);

        const store = mockStore({ adminStore: {}});
        return store.dispatch(
            actions.fetchRunsIfNeeded({
                getAppURLBase() {
                    return baseUrl;
                },
                pipeline: 'xxx'
            }))
            .then(() => { // return of async actions
                assert.equal(currentRunsSelector({adminStore: {currentRuns: latestRuns}}).length, latestRuns.length);
                assert.equal(store.getActions()[0].type, 'CLEAR_CURRENT_RUN_DATA');
            });
    });
});


