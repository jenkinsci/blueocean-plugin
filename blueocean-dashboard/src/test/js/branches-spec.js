import React from 'react';
import {createRenderer} from 'react-addons-test-utils';
import { assert} from 'chai';
import sd from 'skin-deep';
import { latestRuns as data } from './data/runs/latestRuns';
import { RunsRecord } from '../../main/js/components/records.jsx';

import Branches from '../../main/js/components/Branches.jsx';

const t = () => {};

describe("Branches should render", () => {
  let tree;
  const
    renderer = createRenderer();

  beforeEach(() => {
    const branch = new RunsRecord(data[0]);
    tree = sd.shallowRender(<Branches t={t} data={branch} pipeline={{}} />, {
        router: {},
        location: {},
    });
  });

  it("renders the Branches", () => {
    const row = tree.everySubTree('CellLink');
    const lastCol = tree.everySubTree('td');
    const weatherIcon =row[0].getRenderOutput();
    assert.isNotNull(weatherIcon);
    assert.isNotNull(weatherIcon.props.score);
    assert.equal(weatherIcon.props.score, data[0].score);
    // dash for empty or id
    const commitHash = data[0].latestRun.commitId.substr(0, 7);
    const hashComp = row[3].getRenderOutput().props.children;
    const hashRendered = sd.shallowRender(hashComp).getRenderOutput();
    assert.equal(hashRendered.props.children, commitHash);
    assert.equal(row.length, 6);
    assert.equal(lastCol.length, 1);
  });
});

describe("Branches should not render", () => {
  let tree;
  const
    renderer = createRenderer();

  beforeEach(() => {
    tree = sd.shallowRender(<Branches t={t}/>);
  });

  it("renders the Branches", () => {
    assert.isNull(tree.getRenderOutput());
  });
});

