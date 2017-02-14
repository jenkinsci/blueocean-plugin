/**
 * Created by cmeyers on 7/6/16.
 */
import React from 'react';
import { assert } from 'chai';
import { shallow } from 'enzyme';

import { PipelineCard } from '../../../main/js/components/PipelineCard';

function clone(object) {
    return JSON.parse(JSON.stringify(object));
}

const context = {
    params: {},
    config: {
        getServerBrowserTimeSkewMillis: () => 0
    },
    activityService: {
        activityPager() {
            return {
                data: data
            }
        }
    }
};

describe('PipelineCard', () => {
    let item;
    let favorite;

    function shallowRenderCard() {
        return shallow(
            <PipelineCard runnable={item} favorite={favorite} />, context
        );
    }

    beforeEach(() => {
        const favorites = clone(require('../data/favorites.json'));

        item = favorites[0].item;
        item._capabilities = ['io.jenkins.blueocean.rest.model.BlueBranch'];
        favorite = true;
    });

    it('renders without error for empty props', () => {
        const wrapper = shallow(
            <PipelineCard />, context
        );

        assert.isOk(wrapper);
    });

    it('renders basic child elements', () => {
        item.latestRun.result = 'SUCCESS';
        const wrapper = shallowRenderCard();

        assert.equal(wrapper.find('NewComponent').length, 1);
        assert.equal(wrapper.find('.name').length, 1);
        assert.equal(wrapper.find('.name').text(), '<Link />');
        assert.equal(wrapper.find('.branch').length, 1);
        assert.equal(wrapper.find('.branchText').text(), 'UX-301');
        assert.equal(wrapper.find('.commit').length, 1);
        assert.equal(wrapper.find('.commitId').text(), '#cfca303');
        assert.equal(wrapper.find('Favorite').length, 1);
    });

    it('renders "rerun" button after failure', () => {
        item.latestRun.result = 'FAILURE';
        const wrapper = shallowRenderCard();
        const replayButton = wrapper.find('ReplayButton').shallow();

        assert.isOk(replayButton.text());
    });

    it('renders "rerun" button after success', () => {
        item.latestRun.result = 'SUCCESS';
        const wrapper = shallowRenderCard();
        const replayButton = wrapper.find('ReplayButton').shallow();

        assert.isOk(replayButton.text());
    });

    it('renders a "run" button when successful', () => {
        item.latestRun.result = 'SUCCESS';
        const wrapper = shallowRenderCard();
        const pRunButton = wrapper.find('ParametersRunButton').shallow();
        const runButton = pRunButton.find('RunButton').shallow();
        assert.equal(runButton.find('.run-button').length, 1);
    });

    it('renders no "run" button while running', () => {
        item.latestRun.state = 'RUNNING';
        const wrapper = shallowRenderCard();
        const pRunButton = wrapper.find('ParametersRunButton').shallow();
        const runButton = pRunButton.find('RunButton').shallow();
        assert.equal(runButton.find('.run-button').length, 0);
    });

    it('renders a "stop" button while running', () => {
        item.latestRun.state = 'RUNNING';
        const wrapper = shallowRenderCard();
        const pRunButton = wrapper.find('ParametersRunButton').shallow();
        const runButton = pRunButton.find('RunButton').shallow();
        assert.equal(runButton.find('.stop-button').length, 1);
    });

    it('renders no "stop" button after success', () => {
        item.latestRun.state = 'RUNNING';
        const wrapper = shallowRenderCard();
        const pRunButton = wrapper.find('ParametersRunButton').shallow();
        const runButton = pRunButton.find('RunButton').shallow();
        assert.equal(runButton.find('.stop-button').length, 1);
    });

    it('renders "not built" status if no latest run', () => {
        item.latestRun = null;
        const wrapper = shallowRenderCard();

        assert.equal(wrapper.find('.not_built-bg-lite').length, 1);
    });

    it('escapes the branch name', () => {
        const branch = 'experiment/build-locally-docker';
        item.fullName = `jdl1/${encodeURIComponent(branch)}`;
        item.name = encodeURIComponent(branch);
        const wrapper = shallowRenderCard();

        const elements = wrapper.find('.branchText');
        assert.equal(elements.length, 1);
        assert.equal(elements.at(0).text(), branch);
    });
});
