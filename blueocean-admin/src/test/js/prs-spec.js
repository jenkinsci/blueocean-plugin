import React from 'react';
import {createRenderer} from 'react-addons-test-utils';
import { assert} from 'chai';
import sd from 'skin-deep';
import Immutable from 'immutable';
import { latestRuns as data } from './latestRuns'

const pr = data.filter((run) => run.pullRequest);

import {Prs} from '../../main/js/components/Prs.jsx';

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

describe("Prs should render", () => {
  let tree = null;

  beforeEach(() => {
    tree = sd.shallowRender(<Prs
      data={ Immutable.fromJS(data)}
      back={() => {}}
      pipeline={ Immutable.fromJS(pipeline)}/>);
  });

  it("does renders the Prs with data", () => {
    // does WeatherIcon renders the value from the pipeline?
    const weatherIcon = tree.subTree('WeatherIcon').getRenderOutput();
    assert.isNotNull(weatherIcon);
    assert.isNotNull(weatherIcon.props.score);
    // does data renders?
    const runs = tree.subTree('Pr').getRenderOutput();
    assert.isNotNull(runs.props.changeset)
  });

});

describe("Prs should not render", () => {
  let tree = null;

  beforeEach(() => {

    tree = sd.shallowRender(<Prs/>);
  });

  it("does not renders the Prs without data", () => {
    assert.isNull(tree.getRenderOutput());
  });

});
