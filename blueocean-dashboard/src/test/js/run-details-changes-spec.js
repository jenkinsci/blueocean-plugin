import { assert } from 'chai';
import React from 'react';
import { render } from 'enzyme';
import { i18nTranslator } from '@jenkins-cd/blueocean-core-js';

import { latestRuns } from './data/runs/latestRuns';
import RunDetailsChanges from '../../main/js/components/RunDetailsChanges';

const t = i18nTranslator('blueocean-dashboard');

import { mockExtensionsForI18n } from './mock-extensions-i18n';
mockExtensionsForI18n();

const params = {
    organization: 'jenkins',
    pipeline: 'asdf',
};

function buildContext(changeSetPagerData) {
    return {
        activityService: {
            changeSetPager() {
                return {
                    data: changeSetPagerData,
                };
            },
        },
    };
}

describe('RunDetailsChanges', () => {
    beforeAll(() => mockExtensionsForI18n());

    it('renders nothing with no data', () => {
        const wrapper = render(<RunDetailsChanges t={t} params={params} />, { buildContext() });

        assert.equal(wrapper.html(), '', 'output should be empty');
    });

    it('renders empty changeset', () => {
        const wrapper = render(<RunDetailsChanges t={t} params={params} />, { context: buildContext([]) });

        const output = wrapper.html();

        // Class names we expect to see
        assert(output.match('RunDetailsEmpty'), 'output should contain "RunDetailsEmpty"');
        assert(output.match('NoChanges'), 'output should contain "NoChanges"');
        assert(output.match('PlaceholderTable'), 'output should contain "PlaceholderTable"');
    });

    it('renders a valid changeset', () => {
        const runs = latestRuns.map(run => run.latestRun);
        const wrapper = render(<RunDetailsChanges t={t} params={params} />, { context: buildContext(runs.changeSet) });
        const output = wrapper.html();

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
