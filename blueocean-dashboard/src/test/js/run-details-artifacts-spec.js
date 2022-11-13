import { assert } from 'chai';
import React from 'react';
import { mount } from 'enzyme';
import { i18nTranslator } from '@jenkins-cd/blueocean-core-js';

import { latestRuns } from './data/runs/latestRuns';
import { RunDetailsArtifacts } from '../../main/js/components/RunDetailsArtifacts';

const t = i18nTranslator('blueocean-dashboard');

import { mockExtensionsForI18n } from './mock-extensions-i18n';
mockExtensionsForI18n();

function buildContext(artifactPagerData) {
    return {
        /*params: {
            organization: 'jenkins',
            pipeline: 'job1',
            branch: 'master',
            runId: '2',
        },*/
        activityService: {
            artifactsPager() {
                return {
                    data: artifactPagerData,
                };
            },
        },
    };
}

describe('RunDetailsArtifacts', () => {
    beforeAll(() => mockExtensionsForI18n());

    it('renders nothing with no data', () => {
        const wrapper = mount(<RunDetailsArtifacts t={t}/>, { context: buildContext() });

        assert.equal(wrapper.html(), null, 'output should be empty');
    });

    it('renders a valid list of artifacts', () => {
        const runs = latestRuns.map(run => run.latestRun);
        const wrapper = mount(<RunDetailsArtifacts t={t} result={runs[0]} />, { context: buildContext(runs[0].artifacts) });
        const output = wrapper.html();

        // Class names we expect to see
        assert(output.match('JTable'), 'output should contain "JTable"');
        assert(output.match('JTable-row'), 'output should contain "JTable-row"');
        assert(output.match('JTable-cell'), 'output should contain "JTable-cell"');

        // Data values we expect to see
        assert(output.match('hey'), 'output should contain "hey"');
        assert(output.match('href'), 'output should contain "href"');
        assert(output.match('/jenkins/job/jenkinsfile-experiments/branch/master/1/artifact/hey'), 'output should contain "/jenkins/job/jenkinsfile-experiments/branch/master/1/artifact/hey"');
    });
});
