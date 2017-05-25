import React from 'react';
import { assert, expect } from 'chai';
import { render, shallow } from 'enzyme';
import { pipelines } from './data/pipelines/pipelinesSingle';
import { latestRuns as runs } from './data/runs/latestRuns';
import { PipelineRecord, RunsRecord } from '../../main/js/components/records.jsx';
import { CapabilityRecord } from '../../main/js/components/Capability.jsx';

import Branches from '../../main/js/components/Branches.jsx';
import { mockExtensionsForI18n } from './mock-extensions-i18n';

const t = () => {};

const capabilities = {
    'some.class': new CapabilityRecord({}),
};

describe('Branches should render', () => {
    // beforeAll(() => mockExtensionsForI18n());

    it('renders the Branches', () => {
        const pipeline = new PipelineRecord(pipelines[0]);
        const branch = new RunsRecord(runs[0]);
        const wrapper = render(<Branches t={t} data={branch} pipeline={pipeline} capabilities={capabilities} />);

        const weather = wrapper.find('.weather-sunny');
        expect(weather).to.have.length(1);

        const hash = wrapper.find('.hash');
        expect(hash).to.have.length(1);
        assert.equal(hash.text(), '09794ca');

        const message = wrapper.find('.RunMessageCell');
        expect(message).to.have.length(1);
        assert.equal(message.text(), 'Update Jenkinsfile');
    });
});

describe('Branches should not render', () => {
    beforeAll(() => mockExtensionsForI18n());

    it('renders the Branches', () => {
        const wrapper = shallow(<Branches t={t} />);
        assert.equal(wrapper.text(), '');
    });
});

