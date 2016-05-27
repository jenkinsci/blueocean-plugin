import React from 'react';
import { assert} from 'chai';
import { shallow } from 'enzyme';
import configureMockStore from 'redux-mock-store';
import thunk from 'redux-thunk';
import nock from 'nock';

import {
  actions,
  ACTION_TYPES,
  nodes as nodesSelector,
} from '../../main/js/redux';

import { runNodesSuccess, runNodesFail, runNodesRunning } from './runNodes';
import {firstFinishedSecondRunning } from './runNodes-firstFinishedSecondRunning';
import { firstRunning } from './runNodes-firstRunning';
import { finishedMultipleFailure } from './runNodes-finishedMultipleFailure';
import {getStagesInformation} from './../../main/js/util/logDisplayHelper';

import Node from '../../main/js/components/Node';
import Nodes from '../../main/js/components/Nodes';

const middlewares = [thunk];
const mockStore = configureMockStore(middlewares);

const assertResult = (item, {finished = true, failed = false, errors = 0 , running = 0}) => {
  assert.equal(item.isFinished, finished);
  assert.equal(item.isError, failed);
  assert.equal(item.errorNodes ? item.errorNodes.length : 0, errors);
  assert.equal(item.runningNodes ? item.runningNodes.length : 0, running);
};

describe("Logic test of different runs", () => {
  it("handles success", () => {
    const stagesInformationSuccess = getStagesInformation(runNodesSuccess);
    assertResult(stagesInformationSuccess, {});
  });
  it("handles error", () => {
    let stagesInformationFail = getStagesInformation(runNodesFail);
    assertResult(stagesInformationFail, {failed: true, errors: 2});
    stagesInformationFail = getStagesInformation(finishedMultipleFailure);
    assertResult(stagesInformationFail, {failed: true, errors: 3});
  });
  it("handles running", () => {
    const runningSamples = [runNodesRunning, firstRunning, firstFinishedSecondRunning];
    runningSamples
      .map((item) => assertResult(
        getStagesInformation(item),
        {finished: false, failed: null, running: 1}
      ));
  });
});

describe("React component test of different runs", () => {
  it("handles success", () => {
    const wrapper = shallow(
      <Nodes nodeInformation={getStagesInformation(runNodesSuccess)}/>);
    assert.isNotNull(wrapper);
    assert.equal(wrapper.find('Node').length, runNodesSuccess.length)
  });
  it("handles error", () => {
    const wrapper = shallow(
      <Nodes nodeInformation={getStagesInformation(runNodesFail)}/>);
    assert.isNotNull(wrapper);
    assert.equal(wrapper.find('Node').length, runNodesFail.length)
  });
  it("handles error node", () => {
    const wrapper = shallow(
      <Node node={getStagesInformation(runNodesFail).model[2]}/>);
    assert.isNotNull(wrapper);
  });
});

// url for a multiBranch pipeline
// '/rest/organizations/jenkins/pipelines/jdl/branches/experiment%252FUX-38-Flow/runs/3/nodes/'

describe("xxx LogStore should work", () => {
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
    const runNodesInformation = getStagesInformation(runNodesSuccess);
    const model = runNodesInformation.model;
    return store.dispatch(
      actions.generateData('http://example.com' + nodes, ACTION_TYPES.SET_NODES))
      .then(() => { // return of async actions
        assert.equal(store.getActions()[0].type, 'SET_NODES');
        const modelLength = model.length;
        const payload = store.getActions()[0].payload;
        assert.equal(payload.length, modelLength);
        const selector = nodesSelector({adminStore: {nodes: model}});
        assert.equal(selector.length, modelLength);
      });
  });

});


