import React from 'react';
import { assert, expect } from 'chai';
import { render, shallow } from 'enzyme';
import { pipelines } from './data/pipelines/pipelinesSingle';
import { latestRuns as runs } from './data/runs/latestRuns';
import { PipelineRecord, RunsRecord } from '../../main/js/components/records.jsx';
import { CapabilityRecord } from '../../main/js/components/Capability.jsx';

import Branches from '../../main/js/components/Branches.jsx';

const t = () => {};

const capabilities = {
    'some.class': new CapabilityRecord({}),
};

describe('Branches should render', () => {
    it('renders the Branches', () => {
        const pipeline = new PipelineRecord(pipelines[0]);
        const branch = new RunsRecord(runs[0]);
        const wrapper = render(<Branches t={t} data={branch} pipeline={pipeline} capabilities={capabilities} />);

        const weather = wrapper.find('.weather-sunny');
        expect(weather).to.have.length(1);

        const hash = wrapper.find('.hash');
        expect(hash).to.have.length(1);
        assert.equal(hash.text(), '09794ca');

        const message = wrapper.find('.message');
        expect(message).to.have.length(1);
        assert.equal(message.text(), 'Update Jenkinsfile');
    });

    it('renders the branches with cause', () => {
        const pipeline = new PipelineRecord(pipelines[0]);
        const branch = new RunsRecord(runs[1]);
        const wrapper = render(<Branches t={t} data={branch} pipeline={pipeline} capabilities={capabilities} />);
        assert.isNotNull(wrapper);

        expect(wrapper.find('.weather-storm')).to.have.length(1);

        const hash = wrapper.find('.hash');
        assert.isNotNull(hash);
        assert.equal(hash.text(), 'c38ab8e');

        const message = wrapper.find('.message');
        expect(message).to.have.length(1);
        assert.equal(message.text(), 'Branch indexing');
    });
});

describe('Branches should not render', () => {
    it('renders the Branches', () => {
        const wrapper = shallow(<Branches t={t} />);
        assert.equal(wrapper.text(), '');
    });
});

