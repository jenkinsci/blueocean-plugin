/* eslint-disable */
import React, { PropTypes, Component } from 'react';
import { assert } from 'chai';
import { shallow, render, mount } from 'enzyme';
import WithContext from '@jenkins-cd/design-language/dist/js/stories/WithContext';

import moment from 'moment';
import { TimeHarmonizer, TimeHarmonizerUtil } from '../../../src/js';


jest.mock('../../../src/js/i18n/i18n');


class UselessComponent extends Component {

    render() {
        const {
            isRunning,
            startTime,
            endTime,
            durationInMillis,
            getTimes,
            getDuration,
            getI18nTitle,
            result,
        } = this.props;

        const processedTimes = getTimes({
            startTime,
            endTime,
            durationInMillis,
            result
        });

        return (
            <div className="UselessComponent">
                <h3>Input</h3>
                <dl>
                    <dt>startTime</dt>
                    <dd className="in-startTime">{ String(startTime) }</dd>
                    <dt>endTime</dt>
                    <dd className="in-endTime">{ String(endTime) }</dd>
                    <dt>durationInMillis</dt>
                    <dd className="in-durationInMillis">{ String(durationInMillis) }</dd>
                    <dt>isRunning</dt>
                    <dd className="in-isRunning">{ String(isRunning) }</dd>
                </dl>
                <h3>Synced</h3>
                <dl>
                    <dt>startTime</dt>
                    <dd className="syn-startTime">{ String(processedTimes.startTime) }</dd>
                    <dt>endTime</dt>
                    <dd className="syn-endTime">{ String(processedTimes.endTime) }</dd>
                    <dt>durationInMillis</dt>
                    <dd className="syn-durationInMillis">{ String(processedTimes.durationInMillis) }</dd>
                    <dt>getDuration</dt>
                    <dd className="syn-getDuration">{ String(getDuration()) }</dd>
                    <dt>getI18nTitle</dt>
                    <dd className="syn-getI18nTitle">{ String(getI18nTitle(result)) }</dd>
                </dl>
            </div>
        )
    }
}

describe('TimeHarmonizer', () => {
    const HarmonizedUselessComponent = TimeHarmonizer(UselessComponent);

    it('/ renders', () => {
        const wrapper = shallow(
            <HarmonizedUselessComponent/>
        );
    });

    const time1Europe = '2017-01-25T10:28:34.755+0100';
    const time1Zulu = '2017-01-25T09:28:34.755Z';
    const time1ZuluPlus5m = '2017-01-25T09:33:34.755Z';
    const time1ZuluPlus5h = '2017-01-25T14:28:34.755Z';

    const time2BNE = '2017-01-26T12:24:42.557+1000';
    const time2Zulu = '2017-01-26T02:24:42.557Z';
    const time2ZuluPlus10m = '2017-01-26T02:34:42.557Z';
    const time2ZuluPlus5h = '2017-01-26T07:24:42.557Z';

    let oldClock;

    beforeEach(() => {
        oldClock = TimeHarmonizerUtil.timeManager.currentTime;
    });

    afterEach(() => {
        TimeHarmonizerUtil.timeManager.currentTime = oldClock;
    });


    it('/ renders with time props', () => {

        TimeHarmonizerUtil.timeManager.currentTime = () => moment(time1ZuluPlus5m);

        let timeRelatedProps = {
            isRunning: true,
            startTime: time1Europe,
            durationInMillis: 45678,
            endTime: undefined,
            result: 'running',
        };

        let wrapper = mount(<HarmonizedUselessComponent {...timeRelatedProps}/>);

        // Input

        assert.equal(wrapper.find('.in-startTime').length, 1, 'input start time missing');
        assert.equal(wrapper.find('.in-startTime').text(), time1Europe, 'input start time wrong');

        assert.equal(wrapper.find('.in-endTime').length, 1, 'input end time missing');
        assert.equal(wrapper.find('.in-endTime').text(), 'undefined', 'input end time wrong');

        assert.equal(wrapper.find('.in-durationInMillis').length, 1, 'input duration missing');
        assert.equal(wrapper.find('.in-durationInMillis').text(), '45678', 'input duration wrong');

        assert.equal(wrapper.find('.in-isRunning').length, 1, 'input isRunning missing');
        assert.equal(wrapper.find('.in-isRunning').text(), 'true', 'input isRunning wrong');

        // Synchronized

        assert.equal(wrapper.find('.syn-startTime').length, 1, 'sync start time missing');
        assert.equal(wrapper.find('.syn-startTime').text(), time1Zulu, 'sync start time wrong');

        assert.equal(wrapper.find('.syn-endTime').length, 1, 'sync end time missing');
        assert.equal(wrapper.find('.syn-endTime').text(), 'null', 'sync end time wrong');

        assert.equal(wrapper.find('.syn-durationInMillis').length, 1, 'sync duration missing');
        assert.equal(wrapper.find('.syn-durationInMillis').text(), String(5 * 60 * 1000), 'sync duration wrong');

        assert.equal(wrapper.find('.syn-getDuration').length, 1, 'sync duration2 missing');
        assert.equal(wrapper.find('.syn-getDuration').text(), String(5 * 60 * 1000), 'sync duration2 wrong');

        assert.equal(wrapper.find('.syn-getI18nTitle').length, 1, 'sync translated title missing');
        assert.equal(wrapper.find('.syn-getI18nTitle').text(), 'common.state.running', 'sync translated title wrong');
    });

    it('/ renders with time props and context without drift', () => {

        TimeHarmonizerUtil.timeManager.currentTime = () => moment(time2ZuluPlus10m);

        let ctx = {
            config: {
                getServerBrowserTimeSkewMillis: () => {
                    return 0;
                }
            }
        };

        let timeRelatedProps = {
            isRunning: true,
            startTime: time2BNE,
            durationInMillis: 45678,
            endTime: undefined,
            result: 'running',
        };

        let wrapper = mount(
            <WithContext context={ctx}>
                <HarmonizedUselessComponent {...timeRelatedProps}/>
            </WithContext>
        );

        // Input

        assert.equal(wrapper.find('.in-startTime').length, 1, 'input start time missing');
        assert.equal(wrapper.find('.in-startTime').text(), time2BNE, 'input start time wrong');

        assert.equal(wrapper.find('.in-endTime').length, 1, 'input end time missing');
        assert.equal(wrapper.find('.in-endTime').text(), 'undefined', 'input end time wrong');

        assert.equal(wrapper.find('.in-durationInMillis').length, 1, 'input duration missing');
        assert.equal(wrapper.find('.in-durationInMillis').text(), '45678', 'input duration wrong');

        assert.equal(wrapper.find('.in-isRunning').length, 1, 'input isRunning missing');
        assert.equal(wrapper.find('.in-isRunning').text(), 'true', 'input isRunning wrong');

        // Synchronized

        assert.equal(wrapper.find('.syn-startTime').length, 1, 'sync start time missing');
        assert.equal(wrapper.find('.syn-startTime').text(), time2Zulu, 'sync start time wrong');

        assert.equal(wrapper.find('.syn-endTime').length, 1, 'sync end time missing');
        assert.equal(wrapper.find('.syn-endTime').text(), 'null', 'sync end time wrong');

        assert.equal(wrapper.find('.syn-durationInMillis').length, 1, 'sync duration missing');
        assert.equal(wrapper.find('.syn-durationInMillis').text(), String(10 * 60 * 1000), 'sync duration wrong');

        assert.equal(wrapper.find('.syn-getDuration').length, 1, 'sync duration2 missing');
        assert.equal(wrapper.find('.syn-getDuration').text(), String(10 * 60 * 1000), 'sync duration2 wrong');

        assert.equal(wrapper.find('.syn-getI18nTitle').length, 1, 'sync translated title missing');
        assert.equal(wrapper.find('.syn-getI18nTitle').text(), 'common.state.running', 'sync translated title wrong');
    });

    it('/ renders with time props and context with drift and endTime', () => {

        TimeHarmonizerUtil.timeManager.currentTime = () => moment(time2ZuluPlus10m);

        let ctx = {
            config: {
                getServerBrowserTimeSkewMillis: () => {
                    return -5 * 60 * 60 * 1000; // -5 hours
                }
            }
        };

        let timeRelatedProps = {
            isRunning: false,
            startTime: time1Europe,
            durationInMillis: 45678,
            endTime: time2Zulu,
            result: 'completed',
        };

        let wrapper = mount(
            <WithContext context={ctx}>
                <HarmonizedUselessComponent {...timeRelatedProps}/>
            </WithContext>
        );

        // Input

        assert.equal(wrapper.find('.in-startTime').length, 1, 'input start time missing');
        assert.equal(wrapper.find('.in-startTime').text(), time1Europe, 'input start time wrong');

        assert.equal(wrapper.find('.in-endTime').length, 1, 'input end time missing');
        assert.equal(wrapper.find('.in-endTime').text(), time2Zulu, 'input end time wrong');

        assert.equal(wrapper.find('.in-durationInMillis').length, 1, 'input duration missing');
        assert.equal(wrapper.find('.in-durationInMillis').text(), '45678', 'input duration wrong');

        assert.equal(wrapper.find('.in-isRunning').length, 1, 'input isRunning missing');
        assert.equal(wrapper.find('.in-isRunning').text(), 'false', 'input isRunning wrong');

        // Synchronized

        assert.equal(wrapper.find('.syn-startTime').length, 1, 'sync start time missing');
        assert.equal(wrapper.find('.syn-startTime').text(), time1ZuluPlus5h, 'sync start time wrong');

        assert.equal(wrapper.find('.syn-endTime').length, 1, 'sync end time missing');
        assert.equal(wrapper.find('.syn-endTime').text(), time2ZuluPlus5h, 'sync end time wrong');

        assert.equal(wrapper.find('.syn-durationInMillis').length, 1, 'sync duration missing');
        assert.equal(wrapper.find('.syn-durationInMillis').text(), '45678', 'sync duration wrong');

        assert.equal(wrapper.find('.syn-getDuration').length, 1, 'sync duration2 missing');
        assert.equal(wrapper.find('.syn-getDuration').text(), '45678', 'sync duration2 wrong');

        assert.equal(wrapper.find('.syn-getI18nTitle').length, 1, 'sync translated title missing');
        assert.equal(wrapper.find('.syn-getI18nTitle').text(), 'common.state.completed', 'sync translated title wrong');
    });
});
