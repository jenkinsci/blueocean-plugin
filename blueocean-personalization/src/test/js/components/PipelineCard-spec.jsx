/**
 * Created by cmeyers on 7/6/16.
 */
import React from 'react';
import { assert } from 'chai';
import { shallow } from 'enzyme';

import { PipelineCard } from '../../../main/js/components/PipelineCard';

describe('PipelineCard', () => {
    const capabilities = [
        'org.jenkinsci.plugins.workflow.job.WorkflowJob'
    ];

    it('renders without error for empty props', () => {
        const wrapper = shallow(
            <PipelineCard />
        );

        assert.isOk(wrapper);
    });

    it('renders basic child elements', () => {
        const status = 'SUCCESS';
        const wrapper = shallow(
            <PipelineCard capabilities={capabilities} status={status} organization="Jenkins" pipeline="blueocean"
              branch="feature/JENKINS-123" commitId="447d8e1" favorite
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
        const status = 'FAILURE';
        const wrapper = shallow(
            <PipelineCard capabilities={capabilities} status={status} organization="Jenkins" pipeline="blueocean"
              branch="feature/JENKINS-123" commitId="447d8e1" favorite
            />
        );

        assert.equal(wrapper.find('.actions .rerun').length, 1);
    });

    it('renders no "rerun" button after success', () => {
        const status = 'SUCCESS';
        const wrapper = shallow(
            <PipelineCard capabilities={capabilities} status={status} organization="Jenkins" pipeline="blueocean"
              branch="feature/JENKINS-123" commitId="447d8e1" favorite
            />
        );

        assert.equal(wrapper.find('.actions .rerun').length, 0);
    });

    it('escapes the branch name', () => {
        const branchName = 'feature/JENKINS-667';
        const wrapper = shallow(
            <PipelineCard status="SUCCESS" organization="Jenkins" pipeline="blueocean"
              branch={encodeURIComponent(branchName)} commitId="447d8e1"
            />
        );

        const elements = wrapper.find('.branchText');
        assert.equal(elements.length, 1);
        assert.equal(elements.at(0).text(), branchName);
    });
});
