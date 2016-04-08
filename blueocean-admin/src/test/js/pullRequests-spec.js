import React from 'react';
import {createRenderer} from 'react-addons-test-utils';
import { assert} from 'chai';
import sd from 'skin-deep';
import Immutable from 'immutable';
import { latestRuns as data } from './latestRuns';

const pr = data.filter((run) => run.pullRequest);

import {PullRequests} from '../../main/js/components/PullRequests.jsx';

const pipeline = {
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
};

describe("PullRequests should render", () => {
  let tree = null;

  beforeEach(() => {
    tree = sd.shallowRender(<PullRequests
      data={ Immutable.fromJS(data)}
      back={() => {}}
      pipeline={ Immutable.fromJS(pipeline)}/>);
  });

  it("does renders the PullRequests with data", () => {
    // does data renders?
    const runs = tree.subTree('PullRequest').getRenderOutput();
    assert.isNotNull(runs.props.changeset)
  });

});

describe("PullRequests should not render", () => {
  let tree = null;

  beforeEach(() => {

    tree = sd.shallowRender(<PullRequests/>);
  });

  it("does not renders the PullRequests without data", () => {
    assert.isNull(tree.getRenderOutput());
  });

});
