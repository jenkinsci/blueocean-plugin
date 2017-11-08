import { assert } from 'chai';
import React from 'react';
import { shallow, render } from 'enzyme';
import { i18nTranslator } from '@jenkins-cd/blueocean-core-js';

import { latestRuns } from './data/runs/latestRuns';
import { RunDetailsChanges } from '../../main/js/components/RunDetailsChanges';

const t = i18nTranslator('blueocean-dashboard');

import { mockExtensionsForI18n } from './mock-extensions-i18n';
mockExtensionsForI18n();

describe('RunDetailsChanges', () => {
    beforeAll(() => mockExtensionsForI18n());

    it('renders nothing with no data', () => {
        let wrapper = render(<RunDetailsChanges t={t} />);

        assert.equal(wrapper.html(), '', 'output should be empty');
    });

    it('renders empty changeset', () => {
        let wrapper = render(<RunDetailsChanges t={t} result={{ changeSet: [] }} />);

        let output = wrapper.html();

        // Class names we expect to see
        assert(output.match('RunDetailsEmpty'), 'output should contain "RunDetailsEmpty"');
        assert(output.match('NoChanges'), 'output should contain "NoChanges"');
        assert(output.match('PlaceholderTable'), 'output should contain "PlaceholderTable"');
    });

    it('renders a valid changeset', () => {
        let runs = latestRuns.map(run => (run.latestRun));
        let wrapper = render(<RunDetailsChanges t={t} result={runs[0]}/>);
        let output = wrapper.html();

        // Class names we expect to see
        assert(output.match('JTable'), 'output should contain "JTable"');
        assert(output.match('JTable-row'), 'output should contain "JTable-row"');
        assert(output.match('JTable-cell'), 'output should contain "JTable-cell"');

        // Data values we expect to see
        assert(output.match('21552ff'), 'output should contain "21552ff"'); // Commit hash
        assert(output.match('tscherler'), 'output should contain "tscherler"');
        assert(output.match('Update Jenkinsfile'), 'output should contain "Update Jenkinsfile"');
    });
});
