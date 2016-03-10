import React from 'react';
import {createRenderer} from 'react-addons-test-utils';
import { assert} from 'chai';
import sd from 'skin-deep';
import Immutable from 'immutable';


import Dashboard from '../../src/main/js/components/Dashboard.jsx';

const
  pipelines = [{
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
    {
      'displayName': 'beers',
      'name': 'beers',
      'organization': 'jenkins',
      'weatherScore': 0
    }];

describe("Dashboard should render", () => {
  let tree;

  beforeEach(() => {
    tree = sd.shallowRender(<Dashboard pipelines={Immutable.fromJS(pipelines)}/>)
  });

  it("renders the Dashboard", () => {
    const
      page = tree.subTree('Page').getRenderOutput();
    assert.isNotNull(page.props.children[0]);
  });

});
describe("Dashboard should not render", () => {
  let tree;

  beforeEach(() => {
    tree = sd.shallowRender(<Dashboard/>)
  });

  it("renders the Dashboard", () => {
    const
      page = tree.subTree('Page').getRenderOutput();
    assert.isUndefined(page.props.children[0]);
  });

});
