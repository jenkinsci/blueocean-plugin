import React from 'react';
import { assert } from 'chai';
import { shallow } from 'enzyme';
import moment from 'moment';
import MockDate from 'mockdate';

import { ReadableDate } from '../../../src/js/components';

beforeEach(() => MockDate.set('2017-02-01'));
afterEach(() => MockDate.reset());

describe("ReadableDate", () => {

    it("renders dash with no data", () => {
        const wrapper = shallow(<ReadableDate />);

        assert.isTrue(wrapper.equals(
            <span>-</span>
        ));
    });

    it("renders dash with non-ISO-8601 date string", () => {
        const wrapper = shallow(<ReadableDate date="2016/06/06" />);

        assert.isTrue(wrapper.equals(
            <span>-</span>
        ));
    });

    it("renders 'a few seconds ago' with current date", () => {
        const now = moment.utc().toISOString();
        const wrapper = shallow(<ReadableDate date={now} />);

        assert.isTrue(wrapper.is('time'));
        assert.equal(wrapper.text(), 'a few seconds ago');
    });

    it("renders 'an hour ago' with current date minus 60m", () => {
        const hourAgo = moment.utc().subtract(60, 'm').toISOString();
        const wrapper = shallow(<ReadableDate date={hourAgo} />);

        assert.isTrue(wrapper.is('time'));
        assert.equal(wrapper.text(), 'an hour ago');
    });

    it("renders proper tooltip without year for same year date", () => {
        const year = moment().year();
        const isoString = `${year}-06-06T17:57:00.000+0000`;
        const wrapper = shallow(<ReadableDate date={isoString} />);

        assert.equal(wrapper.props().title, 'Jun 06 5:57pm UTC');
    });

    it("renders proper tooltip with year for other year dates", () => {
        const isoString = '1980-06-06T17:57:00.000+0000';
        const wrapper = shallow(<ReadableDate date={isoString} />);

        assert.equal(wrapper.props().title, 'Jun 06 1980 5:57pm UTC');
    });

    it("renders proper tooltip with TZ info for non-UTC dates", () => {
        const isoString = '1980-06-06T17:57:00.000-0400';
        const wrapper = shallow(<ReadableDate date={isoString} />);

        assert.equal(wrapper.props().title, 'Jun 06 1980 5:57pm -04:00');
    });

    it("renders proper tooltip as UTC for dates without TZ", () => {
        const isoString = '1980-06-06T17:57:00.000Z';
        const wrapper = shallow(<ReadableDate date={isoString} />);

        assert.equal(wrapper.props().title, 'Jun 06 1980 5:57pm UTC');
    });

});
