import React from 'react';
import {createRenderer} from 'react-addons-test-utils';
import { assert} from 'chai';
import sd from 'skin-deep';
import Immutable from 'immutable';
import { latestRuns as data } from './latestRuns';
import { RunsRecord } from '../../main/js/components/records.jsx';

import Branches from '../../main/js/components/Branches.jsx';

describe("Branches should render", () => {
  let tree;
  const
    renderer = createRenderer();

  beforeEach(() => {
    const branch = new RunsRecord(data[0]);
    tree = sd.shallowRender(<Branches data={branch} />, {
        router: {},
        pipeline: {},
        location: {},
    });
  });

  it("renders the Branches", () => {
    const
      row = tree.everySubTree('td')
    ;
    const weatherIcon =row[0].getRenderOutput();
    assert.isNotNull(weatherIcon);
    assert.isNotNull(weatherIcon.props.score);
    assert.equal(weatherIcon.props.score, data[0].score);
    // dash for empty or id
    const commitHash = data[0].latestRun.commitId.substr(0, 8);
    const hashComp = row[3].getRenderOutput().props.children;
    const hashRendered = sd.shallowRender(hashComp).getRenderOutput();
    assert.equal(hashRendered.props.children, commitHash);
    assert.equal(row.length, 6);
  });
});

describe("Branches should not render", () => {
  let tree;
  const
    renderer = createRenderer();

  beforeEach(() => {
    tree = sd.shallowRender(<Branches />);
  });

  it("renders the Branches", () => {
    assert.isNull(tree.getRenderOutput());
  });
});

