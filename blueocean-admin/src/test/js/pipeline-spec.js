import React from 'react';
import {createRenderer} from 'react-addons-test-utils';
import { assert} from 'chai';
import sd from 'skin-deep';


import Pipeline from '../../main/js/components/Pipeline.jsx';

const
  hack={
    MultiBranch:()=>{},
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
  pipelineSimple = {
    'displayName': 'beers',
    'name': 'beers',
    'organization': 'jenkins',
    'weatherScore': 0
  },
  testElementSimple = (<Pipeline
      hack={hack}
      pipeline={pipelineSimple}
      simple={true}/>
  ),
  testElementMulti = (<Pipeline
      hack={hack}
      pipeline={pipelineMulti}/>
  );

describe("pipeline component simple rendering", () => {
  const
    renderer = createRenderer();

  before('render element', () => renderer.render(testElementSimple));

  it("renders a pipeline", () => {
    const
      result = renderer.getRenderOutput(),
      children = result.props.children;

    assert.equal(result.type, 'tr');
    assert.equal(children[0].props.children, pipelineSimple.name);
    // simple element has no children
    assert.equal(children[2].type, 'td');
    assert.isObject(children[2].props);
    assert.isUndefined(children[2].props.children);
  });

});

describe("pipeline component multiBranch rendering", () => {
  const
    renderer = createRenderer();

  before('render element', () => renderer.render(testElementMulti));

  it("renders a pipeline", () => {
    const
      result = renderer.getRenderOutput(),
      children = result.props.children;

    assert.equal(result.type, 'tr');
    assert.equal(children[0].props.children, pipelineMulti.name);
    // simple element has no children
    assert.equal(children[2].type, 'td');
    assert.isObject(children[2].props);
    // multiBranch has more information
    assert.isDefined(children[2].props.children);
    assert.equal(children[2].props.children[0], pipelineMulti.numberOfSuccessfulBranches);
    assert.equal(children[2].props.children[2], pipelineMulti.numberOfFailingBranches);
    assert.equal(children[3].props.children[0], pipelineMulti.numberOfSuccessfulPullRequests);
    assert.equal(children[3].props.children[2], pipelineMulti.numberOfFailingPullRequests);
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
