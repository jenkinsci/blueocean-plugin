import React from 'react';
import {createRenderer} from 'react-addons-test-utils';
import { assert} from 'chai';
import sd from 'skin-deep';
import Immutable from 'immutable';


import Dashboard from '../../main/js/components/Dashboard.jsx';

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
      page = tree.subTree('Pipelines').getRenderOutput();
    assert.equal(page.props.pipelines.size, 2);
    assert.isNotNull(page.props.link);
    assert.isNotNull(page.props.hack);
  });

});
describe("Dashboard should not render", () => {
  let tree;

  beforeEach(() => {
    tree = sd.shallowRender(<Dashboard/>);
  });

  it("does not renders the Dashboard if no pipeline", () => {
    assert.isNull(tree.getRenderOutput());
  });

});
