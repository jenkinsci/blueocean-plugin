import React, { Component, PropTypes } from 'react';
import { createRenderer } from 'react-addons-test-utils';
import { shallow } from 'enzyme';
import { assert } from 'chai';
import sd from 'skin-deep';

import PullRequest from '../../main/js/components/PullRequest.jsx';
import { RunsRecord } from '../../main/js/components/records.jsx';
import { latestRuns as data } from './data/runs/latestRuns';

const pr = data.filter((run) => run.pullRequest);

const t = () => {};

describe('PullRequest should render', () => {
    let tree = null;
    beforeEach(() => {
        const immData = new RunsRecord(pr[0]);
        tree = sd.shallowRender(<PullRequest t={t} pr={immData} pipeline={{}} />,{
            router: {},
            location: {},
        });
    });

    it('does renders the PullRequest with data', () => {
        const PRCols = tree.everySubTree('CellLink');
        const tds = tree.everySubTree('td');
        assert.equal(PRCols.length, 5);
        assert.equal(tds.length, 1);
        assert.equal(data.length, 2);
        assert.equal(pr.length, 1);
        const im = new RunsRecord(pr[0]);

    });
});

describe('PullRequest should not render', () => {
    let tree = null;
    beforeEach(() => {
        tree = sd.shallowRender(<PullRequest t={t}/>);
    });

    it('does renders the PullRequest without data', () => {
        assert.isNull(tree.getRenderOutput());
    });
});

describe('PullRequest', () => {
    it('opens correctly', () => {
        const immData = new RunsRecord(pr[0]);
        const pipeline = {
            fullName: 'asdf/blah',
            organization: 'jenkins',
        };
        const tree = shallow(
            <PullRequest
              t={t}
              pr={immData}
              pipeline={pipeline}
            />
        );

        const row = tree.find('CellRow').shallow();
        const cells = row.find('CellLink');
        const cell = cells.at(0);
        assert.equal(cell.props().linkUrl, '/organizations/jenkins/asdf%2Fblah/detail/PR-6/1/pipeline');
    });
});
