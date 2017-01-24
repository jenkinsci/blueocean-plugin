import React from 'react';
import { assert } from 'chai';
import { shallow } from 'enzyme';

import { TimeDuration } from '../../../src/js/components';

describe("TimeDuration", () => {

    it("renders dash with no data", () => {
        const wrapper = shallow(<TimeDuration />);

        assert.isTrue(wrapper.equals(
            <span>-</span>
        ));
    });

    it("renders dash with invalid data", () => {
        const wrapper = shallow(<TimeDuration date="invalid data" />);

        assert.isTrue(wrapper.equals(
            <span>-</span>
        ));
    });

    it("renders 'a few seconds' with 1ms", () => {
        const wrapper = shallow(<TimeDuration millis={2000} />);
        assert.isTrue(wrapper.is('span'));
        assert.equal(wrapper.text(), '2 seconds');
    });

    it("renders 'a few seconds' with 1ms as string", () => {
        const wrapper = shallow(<TimeDuration date="1" />);

        assert.isTrue(wrapper.equals(
            <span>-</span>
        ));
    });

    it("renders '3 hours' with 3.25h", () => {
        const wrapper = shallow(<TimeDuration millis={1000*60*60*3.25} />);

        assert.isTrue(wrapper.is('span'));
        assert.equal(wrapper.text(), '3 hours 15 minutes 0 seconds');
    });

    it("renders a tooltip of '5m, 5s' when supplied value", () => {
        const wrapper = shallow(<TimeDuration millis={1000*60*5+1000*5} />);

        assert.equal(wrapper.props().title, '5m, 5s');
    });

    it("renders a tooltip of '2h, 0m, 5s' when supplied value", () => {
        const wrapper = shallow(<TimeDuration millis={1000*60*60*2+1000*5} />);

        assert.equal(wrapper.props().title, '2h, 0m, 5s');
    });

    it("renders a custom tooltip", () => {
        const wrapper = shallow(<TimeDuration millis={1} hint="Not very long at all." />);

        assert.equal(wrapper.props().title, 'Not very long at all.');
    });

});
