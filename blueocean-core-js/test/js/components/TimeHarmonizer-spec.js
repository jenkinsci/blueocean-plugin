import React, { PropTypes, Component } from 'react';
import { assert } from 'chai';
import { shallow, render, mount } from 'enzyme';

import { TimeHarmonizer } from '../../../src/js';

function dump(obj) {
    const results = {};
    for (let key in obj) {
        let prop = obj[key];
        results[key] = (typeof prop === 'function') ? "[function]" : prop;
    }
    return results;
}


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
        } = this.props;


        const processedTimes = getTimes();

        let propsString = JSON.stringify(dump(this.props), null, 4);
        let contextString = JSON.stringify(dump(context), null, 4);

        // startTime
        // endTime
        // durationInMillis
        // isRunning

        return (
            <div className="UselessComponent">
                <h3>Input</h3>
                <dl>
                    <dt>startTime</dt>
                    <dd className="in-startTime">{ startTime }</dd>
                    <dt>endTime</dt>
                    <dd className="in-endTime">{ endTime }</dd>
                    <dt>durationInMillis</dt>
                    <dd className="in-durationInMillis">{ durationInMillis }</dd>
                    <dt>isRunning</dt>
                    <dd className="in-isRunning">{ isRunning }</dd>
                </dl>
                <h3>Synced</h3>
                <dl>
                    <dt>startTime</dt>
                    <dd className="syn-startTime">{ startTime }</dd>
                    <dt>endTime</dt>
                    <dd className="syn-endTime">{ endTime }</dd>
                    <dt>durationInMillis</dt>
                    <dd className="syn-durationInMillis">{ durationInMillis }</dd>
                    <dt>isRunning</dt>
                    <dd className="syn-isRunning">{ isRunning }</dd>
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

    it('/ renders with time props', () => {

        let timeRelatedProps = {
            isRunning: true,
            startTime: '2017-01-26T10:28:34.755+0100',
            durationInMillis: 45678,
            endTime: undefined,
        };

        let wrapper = mount(<HarmonizedUselessComponent {...timeRelatedProps}/>);

        assert.equal(wrapper.find('.in-startTime').length, 1, 'input start time missing');
        assert.equal(wrapper.find('.in-startTime').text(), '2017-01-26T10:28:34.755+0100', 'input start time wrong');

        assert.equal(wrapper.find('.in-endTime').length, 1, 'input end time missing');
        assert.equal(wrapper.find('.in-endTime').text(), '', 'input end time wrong');

        assert.equal(wrapper.find('.in-durationInMillis').length, 1, 'input duration missing');
        assert.equal(wrapper.find('.in-durationInMillis').text(), '45678', 'input duration wrong');

        // assert.equal(wrapper.find('.in-isRunning').length, 1, 'input isRunning missing');
        // assert.equal(wrapper.find('.in-isRunning').text(), 'true', 'input isRunning wrong');
        // FIXME: What is this for if it's broken in original code? ^^^

    });
});
