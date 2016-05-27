import React, { Component, PropTypes } from 'react';
import { createRenderer } from 'react-addons-test-utils';
import { assert } from 'chai';
import sd from 'skin-deep';
import moment from 'moment';

import PullRequest from '../../main/js/components/PullRequest.jsx';
import { RunsRecord } from '../../main/js/components/records.jsx';
import { latestRuns as data } from './latestRuns';

const pr = data.filter((run) => run.pullRequest);

describe('PullRequest should render', () => {
    let tree = null;
    beforeEach(() => {
        const immData = new RunsRecord(pr[0]);
        tree = sd.shallowRender(<PullRequest pr={immData} />,{
            router: {},
            pipeline: {},
            location: {},
        });
    });

    it('does renders the PullRequest with data', () => {
        const result = tree.everySubTree('td');
        assert.equal(result.length, 5);
        assert.equal(data.length, 2);
        assert.equal(pr.length, 1);
        const im = new RunsRecord(pr[0]);

    });
});

describe('PullRequest should not render', () => {
    let tree = null;
    beforeEach(() => {
        tree = sd.shallowRender(<PullRequest />);
    });

    it('does renders the PullRequest without data', () => {
        assert.isNull(tree.getRenderOutput());
    });
});
