import React from 'react';
import { assert} from 'chai';
import sd from 'skin-deep';


import Pipelines from '../../src/main/js/components/Pipelines.jsx';

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
    }],
  resultArrayHeaders = ['Name', 'Status', 'Branches', 'Pull Requests', undefined]
  ;

describe("pipelines", () => {
  let tree;

  beforeEach(() => {
    tree = sd.shallowRender(React.createElement(Pipelines, {pipelines: pipelines}));
  });

  it("renders pipelines - check header to be as expected", () => {
    const
      header = tree.everySubTree('th'),
      headerArray = header.map(head => {
        var cleanHead = head.text();
        if (cleanHead) {
          return cleanHead;
        }
      })
      ;
    assert.equal(headerArray.length, resultArrayHeaders.length);
  });

  it("renders pipelines - check rows number to be as expected", () => {
    const
      row = tree.everySubTree('Pipeline')
      ;
    assert.equal(row.length, 2);
  });

});
