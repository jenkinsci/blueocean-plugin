/**
 * Created by cmeyers on 7/6/16.
 */
import React from 'react';
import { assert } from 'chai';
import { shallow } from 'enzyme';

import { PipelineCard } from '../../../main/js/components/PipelineCard';

describe('PipelineCard', () => {
    it('renders basic child elements', () => {
        const status = 'SUCCESS';
        const wrapper = shallow(
            <PipelineCard status={status} organization="Jenkins" pipeline="blueocean"
              branch="feature/JENKINS-123" commitId="447d8e1" favorite
            />
        );

        assert.equal(wrapper.find('LiveStatusIndicator').length, 1);
        assert.equal(wrapper.find('.name').length, 1);
        assert.equal(wrapper.find('.name').text(), 'Jenkins / blueocean');
        assert.equal(wrapper.find('.branch').length, 1);
        assert.equal(wrapper.find('.branchText').text(), 'feature/JENKINS-123');
        assert.equal(wrapper.find('.commit').length, 1);
        assert.equal(wrapper.find('.commitId').text(), '#447d8e1');
        assert.equal(wrapper.find('Favorite').length, 1);
    });

    it('renders "run" button after failure', () => {
        const status = 'FAILURE';
        const wrapper = shallow(
            <PipelineCard status={status} organization="Jenkins" pipeline="blueocean"
              branch="feature/JENKINS-123" commitId="447d8e1" favorite
            />
        );

        assert.equal(wrapper.find('.actions .run').length, 1);
    });

    it('renders no "run" button after success', () => {
        const status = 'SUCCESS';
        const wrapper = shallow(
            <PipelineCard status={status} organization="Jenkins" pipeline="blueocean"
              branch="feature/JENKINS-123" commitId="447d8e1" favorite
            />
        );

        assert.equal(wrapper.find('.actions .run').length, 0);
    });
});
