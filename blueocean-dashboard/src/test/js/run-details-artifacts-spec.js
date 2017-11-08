import { assert } from 'chai';
import React from 'react';
import { shallow, mount } from 'enzyme';

import { latestRuns } from './data/runs/latestRuns';
import { RunDetailsArtifacts } from '../../main/js/components/RunDetailsArtifacts';

const runs = latestRuns.map(run => (run.latestRun));


const contextWithArtifacts = {
    activityService: {
        fetchArtifacts() {
            return { value: runs[0].artifacts };
        },
    },
};

const contextNoData = {
    activityService: {
        fetchArtifacts() {
            return { value: [] };
        },
    },
};

const t = () => {};
describe('RunDetailsArtifacts', () => {
    describe('bad data', () => {
        it('renders nothing', () => {
            const wrapper = shallow(<RunDetailsArtifacts t={t} />);
            assert.isNull(wrapper.get(0));
        });
    });

    describe('valid artifacts', () => {
        it('renders a Table with expected data', () => {
            const wrapper = shallow(<RunDetailsArtifacts t={t} result={runs[0]} />, { context: contextWithArtifacts });

            assert.equal(wrapper.find('JTable').length, 1);
            assert.equal(wrapper.find('TableRow').length, 2);

            const cols = wrapper.find('TableCell');
            assert.equal(cols.length, 6);

            assert.equal(cols.at(0).children().text(), 'pipeline.log');
            assert.equal(cols.at(1).children().text(), '-');
            assert.equal(cols.at(2).children().text(), '<Icon />');

            assert.equal(cols.at(3).children().text(), 'hey');
            assert.equal(cols.at(4).children().text(), '<FileSize />');
            assert.equal(cols.at(5).children().text(), '<Icon />');
        });
    });
});
