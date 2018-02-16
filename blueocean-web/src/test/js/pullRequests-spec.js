import React from 'react';
import { assert } from 'chai';
import { shallow } from 'enzyme';
import { latestRuns as branches } from './data/runs/latestRuns';

import { PullRequests } from '../../main/js/components/PullRequests.jsx';

import { mockExtensionsForI18n } from './mock-extensions-i18n';

const pr = branches.filter((run) => run.pullRequest);
const pipeline = {
    _class: 'someclass',
    _capabilities: ['io.jenkins.blueocean.rest.model.BlueMultiBranchPipeline'],
};
const t = () => {
};

const context = {
    pipelineService: {
        prPager() {
            return {
                data: pr,
            };
        },
    },
};

const params = {};

describe('PullRequests', () => {

    beforeAll(() => mockExtensionsForI18n());

    describe('/ should render', () => {
        it('does renders the PullRequests with data', () => {
            const wrapper = shallow(<PullRequests t={t} pipeline={pipeline} params={params} />, { context });

            // does data renders?
            assert.equal(wrapper.find('PullRequestRow').length, pr.length);
        });

    });

    describe('/ should not render', () => {
        it('does render NotSupported the PullRequests without data', () => {
            const wrapper = shallow(<PullRequests t={t} />);
            assert.equal(wrapper.find('UnsupportedPlaceholder').length, 1);
        });

    });
});


