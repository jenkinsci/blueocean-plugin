/**
 * Created by cmeyers on 7/6/16.
 */
import React from 'react';
import { assert } from 'chai';
import { shallow } from 'enzyme';

import { PipelineCard } from '../../../main/js/components/PipelineCard';

describe('PipelineCard', () => {
    let item;

    beforeEach(() => {
        item = {
            _capabilities: ['org.jenkinsci.plugins.workflow.job.WorkflowJob'],
            status: null,
            organization: 'Jenkins',
            pipeline: 'blueocean',
            branch: 'feature/JENKINS-123',
            commitId: '447d8e1',
            favorite: true,
        };
    });

    it('renders without error for empty props', () => {
        const wrapper = shallow(
            <PipelineCard />
        );

        assert.isOk(wrapper);
    });

    it('renders basic child elements', () => {
        item.status = 'SUCCESS';
        const wrapper = shallow(
            <PipelineCard item={item} status={item.status} organization={item.organization} pipeline={item.pipeline}
              branch={item.branch} commitId={item.commitId} favorite={item.favorite}
            />
        );

        assert.equal(wrapper.find('LiveStatusIndicator').length, 1);
        assert.equal(wrapper.find('.name').length, 1);
        assert.equal(wrapper.find('.name').text(), '<Link />');
        assert.equal(wrapper.find('.branch').length, 1);
        assert.equal(wrapper.find('.branchText').text(), 'feature/JENKINS-123');
        assert.equal(wrapper.find('.commit').length, 1);
        assert.equal(wrapper.find('.commitId').text(), '#447d8e1');
        assert.equal(wrapper.find('Favorite').length, 1);
    });

    it('renders "rerun" button after failure', () => {
        item.status = 'FAILURE';
        const wrapper = shallow(
            <PipelineCard item={item} status={item.status} organization={item.organization} pipeline={item.pipeline}
              branch={item.branch} commitId={item.commitId} favorite={item.favorite}
            />
        );

        assert.equal(wrapper.find('.actions .rerun-button').length, 1);
    });

    it('renders no "rerun" button after success', () => {
        item.status = 'SUCCESS';
        const wrapper = shallow(
            <PipelineCard item={item} status={item.status} organization={item.organization} pipeline={item.pipeline}
              branch={item.branch} commitId={item.commitId} favorite={item.favorite}
            />
        );

        assert.equal(wrapper.find('.actions .rerun-button').length, 0);
    });

    it('renders a "run" button when successful', () => {
        item.status = 'SUCCESS';
        const wrapper = shallow(
            <PipelineCard item={item} status={item.status} organization={item.organization} pipeline={item.pipeline}
              branch={item.branch} commitId={item.commitId} favorite={item.favorite}
            />
        );

        assert.equal(wrapper.find('.actions .run-button').length, 1);
    });

    it('renders no "run" button while running', () => {
        item.status = 'RUNNING';
        const wrapper = shallow(
            <PipelineCard item={item} status={item.status} organization={item.organization} pipeline={item.pipeline}
              branch={item.branch} commitId={item.commitId} favorite={item.favorite}
            />
        );

        assert.equal(wrapper.find('.actions .run-button').length, 0);
    });

    it('renders a "stop" button while running', () => {
        item.status = 'RUNNING';
        const wrapper = shallow(
            <PipelineCard item={item} status={item.status} organization={item.organization} pipeline={item.pipeline}
              branch={item.branch} commitId={item.commitId} favorite={item.favorite}
            />
        );

        assert.equal(wrapper.find('.actions .stop-button').length, 1);
    });

    it('renders no "stop" button after success', () => {
        item.status = 'SUCCESS';
        const wrapper = shallow(
            <PipelineCard item={item} status={item.status} organization={item.organization} pipeline={item.pipeline}
              branch={item.branch} commitId={item.commitId} favorite={item.favorite}
            />
        );

        assert.equal(wrapper.find('.actions .stop-button').length, 0);
    });

    it('escapes the branch name', () => {
        item.branch = encodeURIComponent('feature/JENKINS-667');
        const wrapper = shallow(
            <PipelineCard item={item} status={item.status} organization={item.organization} pipeline={item.pipeline}
              branch={item.branch} commitId={item.commitId} favorite={item.favorite}
            />
        );

        const elements = wrapper.find('.branchText');
        assert.equal(elements.length, 1);
        assert.equal(elements.at(0).text(), decodeURIComponent(item.branch));
    });
});
