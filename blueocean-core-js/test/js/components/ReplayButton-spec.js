/**
 * Created by cmeyers on 9/16/16.
 */

import React from 'react';
import { assert } from 'chai';
import { shallow } from 'enzyme';

import utils from '../../../src/js/utils';
import { ReplayButton } from '../../../src/js/components/ReplayButton';

describe('ReplayButton', () => {
    let pipeline;

    beforeEach(() => {
        pipeline = utils.clone(require('../data/pipeline-1.json'));
        pipeline._capabilities = ['org.jenkinsci.plugins.workflow.job.WorkflowJob'];
    });

    it('renders without errors when no props are specified', () => {
        const wrapper = shallow(<ReplayButton />);

        assert.isOk(wrapper);
    });

    describe('in failed state', () => {
        it('renders correctly when permissions are valid', () => {
            pipeline.permissions.start = true;
            pipeline.latestRun.result = 'FAILURE';
            const wrapper = shallow(<ReplayButton runnable={pipeline} latestRun={pipeline.latestRun} />);

            assert.isOk(wrapper);
            assert.equal(wrapper.find('.replay-button-component').length, 1);
            assert.equal(wrapper.find('.replay-button').length, 1);
        });

        it('does not render when permissions are invalid', () => {
            pipeline.permissions.start = false;
            pipeline.latestRun.result = 'FAILURE';
            const wrapper = shallow(<ReplayButton runnable={pipeline} latestRun={pipeline.latestRun} />);

            assert.isOk(wrapper);
            assert.equal(wrapper.find('.replay-button-component').length, 0);
            assert.equal(wrapper.find('.replay-button').length, 0);
        });
    });

    describe('in non-failed state', () => {
        it('renders correctly when permissions are valid', () => {
            pipeline.permissions.start = true;
            const wrapper = shallow(<ReplayButton runnable={pipeline} latestRun={pipeline.latestRun} />);

            assert.isOk(wrapper);
            assert.equal(wrapper.find('.replay-button-component').length, 1);
            assert.equal(wrapper.find('.replay-button').length, 1);
        });

        it('does not render when permissions are invalid', () => {
            pipeline.permissions.start = false;
            const wrapper = shallow(<ReplayButton runnable={pipeline} latestRun={pipeline.latestRun} />);

            assert.isOk(wrapper);
            assert.equal(wrapper.find('.replay-button-component').length, 0);
            assert.equal(wrapper.find('.replay-button').length, 0);
        });
    });
});
