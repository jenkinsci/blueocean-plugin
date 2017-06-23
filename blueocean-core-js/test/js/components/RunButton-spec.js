import React from 'react';
import { assert } from 'chai';
import { shallow } from 'enzyme';

import utils from '../../../src/js/utils';
import { RunButton } from '../../../src/js/components/RunButton';


jest.mock('../../../src/js/i18n/i18n');


describe('RunButton', () => {
    let pipeline;

    beforeEach(() => {
        pipeline = utils.clone(require('../data/pipeline-1.json'));
    });

    it('renders without errors when no props are specified', () => {
        const wrapper = shallow(<RunButton />);

        assert.isOk(wrapper);
    });

    describe('run button', () => {
        describe('in non-running state', () => {
            it('renders correctly when permissions are valid', () => {
                pipeline.permissions.start = true;
                const wrapper = shallow(<RunButton runnable={pipeline} latestRun={pipeline.latestRun} />);

                assert.isOk(wrapper);
                assert.equal(wrapper.find('.run-button-component').length, 1);
                assert.equal(wrapper.find('.run-button').length, 1);
                assert.equal(wrapper.find('.stop-button').length, 0);
            });

            it('does not render when permissions are invalid', () => {
                pipeline.permissions.start = false;
                const wrapper = shallow(<RunButton runnable={pipeline} latestRun={pipeline.latestRun} />);

                assert.isOk(wrapper);
                assert.equal(wrapper.find('.run-button-component').length, 0);
                assert.equal(wrapper.find('.run-button').length, 0);
                assert.equal(wrapper.find('.stop-button').length, 0);
            });
        });

        describe('running state', () => {
            beforeEach(() => {
                pipeline.latestRun.state = 'RUNNING';
            });

            it('renders correctly when permissions are valid', () => {
                pipeline.permissions.stop = true;
                const wrapper = shallow(<RunButton runnable={pipeline} latestRun={pipeline.latestRun} />);

                assert.isOk(wrapper);
                assert.equal(wrapper.find('.run-button-component').length, 1);
                assert.equal(wrapper.find('.run-button').length, 0);
                assert.equal(wrapper.find('.stop-button').length, 1);
            });

            it('does not render when permissions are invalid', () => {
                pipeline.permissions.stop = false;
                const wrapper = shallow(<RunButton runnable={pipeline} latestRun={pipeline.latestRun} />);

                assert.isOk(wrapper);
                assert.equal(wrapper.find('.run-button-component').length, 0);
                assert.equal(wrapper.find('.run-button').length, 0);
                assert.equal(wrapper.find('.stop-button').length, 0);
            });
        });
    });
});
