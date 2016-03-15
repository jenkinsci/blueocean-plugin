import React, { Component, PropTypes } from 'react';
import { createRenderer } from 'react-addons-test-utils';
import { assert } from 'chai';
import sd from 'skin-deep';
import Immutable from 'immutable';
import moment from 'moment';

import Pr from '../../main/js/components/Pr.jsx';
import { runsRecords } from '../../main/js/components/records.jsx';
import { latestRuns as data } from './latestRuns'

const pr = data.filter((run) => run.pullRequest);

describe('PR should render', () => {
    let tree = null;
    beforeEach(() => {
        const immData = new runsRecords(pr[0]);
        tree = sd.shallowRender(<Pr pr={immData} />);
    });

    it('does renders the PR with data', () => {
        const result = tree.everySubTree('td');
        assert.equal(result.length, 5);
        assert.equal(data.length, 2);
        assert.equal(pr.length, 1);
        const im = new runsRecords(pr[0]);

    });
});

describe('PR should not render', () => {
    let tree = null;
    beforeEach(() => {
        tree = sd.shallowRender(<Pr />);
    });

    it('does renders the PR without data', () => {
        assert.isNull(tree.getRenderOutput());
    });
});
