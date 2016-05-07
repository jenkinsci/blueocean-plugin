import React from 'react';
import {createRenderer} from 'react-addons-test-utils';
import { assert} from 'chai';
import sd from 'skin-deep';

import PipelineRowItem from '../../main/js/components/PipelineRowItem.jsx';
import { PipelineRecord } from '../../main/js/components/records.jsx';

const
  hack={
    MultiBranch:()=>{},
    Pr:()=>{},
    Activity:()=>{},
  } ,
  pipelineMulti = {
    'displayName': 'moreBeers',
    'name': 'morebeers',
    'organization': 'jenkins',
    'weatherScore': 0,
    'branchNames': ['master'],
    'numberOfFailingBranches': 1,
    'numberOfFailingPullRequests': 0,
    'numberOfSuccessfulBranches': 0,
    'numberOfSuccessfulPullRequests': 0,
    'totalNumberOfBranches': 1,
    'totalNumberOfPullRequests': 0
  },
  pipelineMultiSuccess = {
    'displayName': 'moreBeersSuccess',
    'name': 'morebeersSuccess',
    'organization': 'jenkins',
    'weatherScore': 0,
    'branchNames': ['master'],
    'numberOfFailingBranches': 0,
    'numberOfFailingPullRequests': 0,
    'numberOfSuccessfulBranches': 3,
    'numberOfSuccessfulPullRequests': 3,
    'totalNumberOfBranches': 3,
    'totalNumberOfPullRequests': 3
  },
  pipelineSimple = {
    'displayName': 'beers',
    'name': 'beers',
    'organization': 'jenkins',
    'weatherScore': 0
  },
  testElementSimple = (<PipelineRowItem
      hack={hack}
      pipeline={pipelineSimple}
      simple={true}/>
  ),
  testElementMultiSuccess = (<PipelineRowItem
      hack={hack}
      pipeline={pipelineMultiSuccess}
      />
  ),
  testElementMulti = (<PipelineRowItem
      hack={hack}
      pipeline={pipelineMulti}/>
  );

describe("PipelineRecord can be created ", () => {
    it("without error", () => {
        const pipeRecord = new PipelineRecord(pipelineMultiSuccess);
    })
});

describe("pipeline component simple rendering", () => {
  const
    renderer = createRenderer();

  before('render element', () => renderer.render(testElementSimple));

  it("renders a pipeline", () => {
    const
      result = renderer.getRenderOutput(),
      children = result.props.children;

    assert.equal(result.type, 'tr');
    assert.equal(children[0].props.children.props.children, pipelineSimple.name);
    // simple element has no children
    assert.equal(children[2].type, 'td');
    assert.isObject(children[2].props);
    assert.equal(children[2].props.children, ' - ');
  });
});

describe("pipeline component multiBranch rendering", () => {
  const
    renderer = createRenderer();

  before('render element', () => renderer.render(testElementMulti));

  it("renders a pipeline with error branch", () => {
    const
      result = renderer.getRenderOutput(),
      children = result.props.children;

    assert.equal(result.type, 'tr');
    assert.equal(children[0].props.children.props.children, pipelineMulti.name);
    // simple element has no children
    assert.equal(children[2].type, 'td');
    assert.isObject(children[2].props);
    // multiBranch has more information
    assert.isDefined(children[2].props.children);
    assert.equal(children[2].props.children.props.children[0], pipelineMulti.numberOfFailingBranches);
  });

});

describe("pipeline component multiBranch rendering - success", () => {
  const
    renderer = createRenderer();

  before('render element', () => renderer.render(testElementMultiSuccess));

  it("renders a pipeline with success branch", () => {
    const
      result = renderer.getRenderOutput(),
      children = result.props.children;

    assert.equal(result.type, 'tr');
    assert.equal(children[0].props.children.props.children, pipelineMultiSuccess.name);
    // simple element has no children
    assert.equal(children[2].type, 'td');
    assert.isObject(children[2].props);
    // multiBranch has more information
    assert.isDefined(children[2].props.children);
    assert.equal(children[2].props.children.props.children[0], pipelineMultiSuccess.numberOfSuccessfulBranches);
  });

});

describe("weatherIcon pipeline component simple rendering", () => {
  const
    renderer = createRenderer();

  before('render element', () => renderer.render(testElementSimple));

  it("renders a weather-icon", () => {
    const
      result = renderer.getRenderOutput(),
      children = result.props.children,
      tree = sd.shallowRender(children[1].props.children),
      vdom = tree.getRenderOutput();

    assert.oneOf('weather-icon', vdom.props.className.split(' '));
  });

});
