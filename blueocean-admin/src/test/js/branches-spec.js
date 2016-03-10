import React from 'react';
import {createRenderer} from 'react-addons-test-utils';
import { assert} from 'chai';
import sd from 'skin-deep';


import {Branche} from '../../main/js/components/Branches.jsx';

const
  pipeline = {
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

describe("Branches should render", () => {
  let tree;
  const
    renderer = createRenderer();

  beforeEach(() => {
    tree = sd.shallowRender(<Branche pipeline={pipeline} branch={pipeline.branchNames[0]}/>);
  });

  it("renders the Branches", () => {
    const
      row = tree.everySubTree('td')
    ;
    assert.equal(row.length, 6);
  });

});

