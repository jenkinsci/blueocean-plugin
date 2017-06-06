import React from 'react';
import { assert } from 'chai';
import { shallow } from 'enzyme';
import configureMockStore from 'redux-mock-store';
import thunk from 'redux-thunk';
import { TestUtils } from '@jenkins-cd/blueocean-core-js';
TestUtils.patchFetchNoJWT();

import { runNodesSuccess, runNodesFail, runNodesRunning } from './data/runs/nodes/runNodes';
import { firstFinishedSecondRunning } from './data/runs/nodes/runNodes-firstFinishedSecondRunning';
import { firstRunning } from './data/runs/nodes/runNodes-firstRunning';
import { finishedMultipleFailure } from './data/runs/nodes/runNodes-finishedMultipleFailure';
import { queuedAborted } from './data/runs/nodes/runNodes-QueuedAborted';
import { getNodesInformation } from './../../main/js/util/logDisplayHelper';
import runningFailing from './data/steps/failingRunningSteps';
import { poststagefail } from './data/runs/nodes/poststagefail';
import { nullNodes } from './data/runs/nodes/nodesAllNull';
import stepsDescriptions from './data/runs/nodes/steps/steps-descriptions';


import Step from '../../main/js/components/karaoke/components/Step';
import Steps from '../../main/js/components/karaoke/components/Steps';

const middlewares = [thunk];
const mockStore = configureMockStore(middlewares);

const assertResult = (item, {finished = true, failed = false, errors = 0, running = 0}) => {
    assert.equal(item.isFinished, finished);
    assert.equal(item.isError, failed);
    assert.equal(item.errorNodes ? item.errorNodes.length : 0, errors);
    assert.equal(item.runningNodes ? item.runningNodes.length : 0, running);
};

describe("Logic test of different runs", () => {
    it('running and failing', () => {
       const stagesInformationRunningFailing = getNodesInformation(runningFailing);
       assert.equal(stagesInformationRunningFailing.model[2].isFocused, true);
    });
    it('post error stage', () => {
       const stagesInformationRunningFailing = getNodesInformation(poststagefail);
       assert.equal(stagesInformationRunningFailing.model[0].isFocused, true);
    });

    it("handles aborted job that only had been in queue but never build", () => {
        const stagesInformationQueuedAborted = getNodesInformation(queuedAborted);
        assert.equal(stagesInformationQueuedAborted.hasResultsForSteps, false);
    });
    it("handles success", () => {
        const stagesInformationSuccess = getNodesInformation(runNodesSuccess);
        assert.equal(stagesInformationSuccess.hasResultsForSteps, true);
        assertResult(stagesInformationSuccess, {});
    });
    it("handles error", () => {
        let stagesInformationFail = getNodesInformation(runNodesFail);
        assertResult(stagesInformationFail, {failed: true, errors: 2});
        assert.equal(stagesInformationFail.hasResultsForSteps, true);
        stagesInformationFail = getNodesInformation(finishedMultipleFailure);
        assertResult(stagesInformationFail, {failed: true, errors: 3});
        assert.equal(stagesInformationFail.hasResultsForSteps, true);
    });
    it("handles running", () => {
        const runningSamples = [runNodesRunning, firstRunning, firstFinishedSecondRunning];
        runningSamples
            .map((item, index) => assertResult(
                getNodesInformation(item),
                {finished: false, failed: null, running: index === 2 ? 3 : 1}
            ));
    });
    it("handles all null", () => {
        const stagesInfo = getNodesInformation(nullNodes);
        assertResult(stagesInfo, {
            finished: false,
            failed: null,
            running: 0,
            errors: 0,
        });
       assert.equal(stagesInfo.model[0].isFocused, true);
    });
    it('includes name and description', () => {
        const stepsInfo = getNodesInformation(stepsDescriptions);
        assert.isOk(stepsInfo);
        assert.isOk(stepsInfo.model);
        stepsInfo.model.forEach(step => {
            assert.isOk(step.displayName);
            assert.isOk(step.displayDescription);
        });
    });
});

describe("React component test of different runs", () => {
    it("handles success", () => {
        const wrapper = shallow(
            <Steps nodeInformation={getNodesInformation(runNodesSuccess)}/>);
        assert.isNotNull(wrapper);
        assert.equal(wrapper.find('Step').length, runNodesSuccess.length)
    });
    it("handles error", () => {
        const wrapper = shallow(
            <Steps nodeInformation={getNodesInformation(runNodesFail)}/>);
        assert.isNotNull(wrapper);
        assert.equal(wrapper.find('Step').length, runNodesFail.length)
    });
});
/*
describe("LogStore should work", () => {
    afterEach(() => {
        nock.cleanAll()
    })
    it("create logStore with log data", () => {
        // url for a simple pipeline (as in not multiBranch)
        var nodes = '/rest/organizations/jenkins/pipelines/Pipeline/runs/22/nodes/';
        var olId = '15/log/';
        nock('http://example.com/')
            .get(nodes)
            .reply(200, runNodesSuccess);
        nock('http://example.com/')
            .get(nodes + olId)
            .reply(200, `Hello World 2 parallel
[workspace] Running shell script
+ date
Tue May 24 13:42:18 CEST 2016
+ sleep 20
+ date
Tue May 24 13:42:38 CEST 2016
`);
        const store = mockStore({adminStore: {logs: []}});
        const runNodesInformation = getNodesInformation(runNodesSuccess);
        const model = runNodesInformation.model;
        return store.dispatch(
            actions.generateData('http://example.com' + nodes, ACTION_TYPES.SET_STEPS))
            .then(() => { // return of async actions
                assert.equal(store.getActions()[0].type, 'SET_STEPS');
                const modelLength = model.length;
                const payload = store.getActions()[0].payload;
                assert.equal(payload.length, modelLength);
                const selector = stepsSelector({adminStore: {steps: model}});
                assert.equal(selector.length, modelLength);
            });
    });

});
*/


