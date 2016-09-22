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

describe('PipelineCard', () => {
    let item;
    let status;
    let organization;
    let pipeline;
    let branch;
    let commitId;
    let favorite;

    function shallowRenderCard() {
        return shallow(
            <PipelineCard item={item} latestRun={item.latestRun} status={status} organization={organization} pipeline={pipeline}
              branch={branch} commitId={commitId} favorite={favorite}
            />
        );
    }

    beforeEach(() => {
        const favorites = clone(require('../data/favorites.json'));

        item = favorites[0].item;
        item._capabilities = ['org.jenkinsci.plugins.workflow.job.WorkflowJob'];
        status = null;
        organization = item.organization;
        pipeline = item.fullName.split('/')[0];
        branch = item.name;
        commitId = item.latestRun.commitId;
        favorite = true;
    });

    it('renders without error for empty props', () => {
        const wrapper = shallow(
            <PipelineCard />
        );

        assert.isOk(wrapper);
    });

    it('renders basic child elements', () => {
        status = 'SUCCESS';
        const wrapper = shallowRenderCard();

        assert.equal(wrapper.find('LiveStatusIndicator').length, 1);
        assert.equal(wrapper.find('.name').length, 1);
        assert.equal(wrapper.find('.name').text(), '<Link />');
        assert.equal(wrapper.find('.branch').length, 1);
        assert.equal(wrapper.find('.branchText').text(), 'UX-301');
        assert.equal(wrapper.find('.commit').length, 1);
        assert.equal(wrapper.find('.commitId').text(), '#cfca303');
        assert.equal(wrapper.find('Favorite').length, 1);
    });

    it('renders "rerun" button after failure', () => {
        status = item.latestRun.result = 'FAILURE';
        const wrapper = shallowRenderCard();
        const replayButton = wrapper.find('ReplayButton').shallow();

        assert.isOk(replayButton.text());
    });

    it('renders no "rerun" button after success', () => {
        status = item.latestRun.result = 'SUCCESS';
        const wrapper = shallowRenderCard();
        const replayButton = wrapper.find('ReplayButton').shallow();

        assert.isNotOk(replayButton.text());
    });

    it('renders a "run" button when successful', () => {
        status = item.latestRun.result = 'SUCCESS';
        const wrapper = shallowRenderCard();
        const runButton = wrapper.find('RunButton').shallow();

        assert.equal(runButton.find('.run-button').length, 1);
    });

    it('renders no "run" button while running', () => {
        status = item.latestRun.state = 'RUNNING';
        const wrapper = shallowRenderCard();
        const runButton = wrapper.find('RunButton').shallow();

        assert.equal(runButton.find('.run-button').length, 0);
    });

    it('renders a "stop" button while running', () => {
        status = item.latestRun.state = 'RUNNING';
        const wrapper = shallowRenderCard();
        const runButton = wrapper.find('RunButton').shallow();

        assert.equal(runButton.find('.stop-button').length, 1);
    });

    it('renders no "stop" button after success', () => {
        status = item.latestRun.state = 'RUNNING';
        const wrapper = shallowRenderCard();
        const runButton = wrapper.find('RunButton').shallow();

        assert.equal(runButton.find('.stop-button').length, 1);
    });

    it('escapes the branch name', () => {
        branch = encodeURIComponent('feature/JENKINS-667');
        const wrapper = shallowRenderCard();

        const elements = wrapper.find('.branchText');
        assert.equal(elements.length, 1);
        assert.equal(elements.at(0).text(), decodeURIComponent(branch));
    });
});
