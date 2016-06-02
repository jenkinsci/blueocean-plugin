import React from 'react';
import { assert } from 'chai';
import { shallow } from 'enzyme';

import { ReadableDate } from '../../../src/js/components';

describe("ReadableDate", () => {

    it("renders dash with no data", () => {
        const wrapper = shallow(<ReadableDate />);

        assert.equal(wrapper.containsMatchingElement(
            <span>-</span>
        ), true);
    });

    it("renders dash with non-ISO-8601 date string", () => {
        const wrapper = shallow(<ReadableDate date="2016/06/06" />);

        assert.equal(wrapper.containsMatchingElement(
            <span>-</span>
        ), true);
    });

    it("renders 'a few seconds ago' with current date", () => {
        const wrapper = shallow(<ReadableDate date={new Date().toISOString()} />);

        assert.equal(wrapper.find('time').length, 1);
        assert.equal(wrapper.text(), 'a few seconds ago');
    });

    it("renders 'an hour ago' with current date minus 60m", () => {
        const now = new Date();
        const hourAgo = new Date(now.getTime() - 1000*60*60);
        const wrapper = shallow(<ReadableDate date={hourAgo.toISOString()} />);

        assert.equal(wrapper.find('time').length, 1);
        assert.equal(wrapper.text(), 'an hour ago');
    });

});