import React from 'react';
import { assert } from 'chai';
import { shallow } from 'enzyme';

import { TimeDuration } from '../../../src/js/components';

describe('TimeDuration', () => {
    const strings = {
        'common.date.duration.display.format': 'd[d] h[h] m[m] s[s]',
        'common.date.duration.format': 'd[d] h[h] m[m] s[s]',
        'common.date.duration.hint.format': 'M [month], d [days], h[ hours], m[ minutes], s[ seconds]',
    };

    const t = key => strings[key] || key;

    it('renders dash with no data', () => {
        const wrapper = shallow(<TimeDuration />);

        assert.isTrue(wrapper.equals(<span>-</span>));
    });

    it('renders dash with invalid data', () => {
        const wrapper = shallow(<TimeDuration date="invalid data" />);

        assert.isTrue(wrapper.equals(<span>-</span>));
    });

    it("renders '2s' with 2000ms", () => {
        const wrapper = shallow(<TimeDuration millis={2000} t={t} />);
        assert.isTrue(wrapper.is('span'));
        assert.equal(wrapper.text(), '2s');
    });

    it("renders '-' with 1 as string", () => {
        const wrapper = shallow(<TimeDuration date="1" t={t} />);

        assert.isTrue(wrapper.equals(<span>-</span>));
    });

    it("renders '3d 15m 0s' with 3.25h", () => {
        const wrapper = shallow(<TimeDuration millis={1000 * 60 * 60 * 3.25} t={t} />);

        assert.isTrue(wrapper.is('span'));
        assert.equal(wrapper.text(), '3h 15m 0s');
    });

    it("renders '<1s' with durations less than 1s", () => {
        const wrapper = shallow(<TimeDuration millis={999} t={t} />);

        assert.isTrue(wrapper.is('span'));
        assert.equal(wrapper.text(), '<1s');
    });

    it('renders correct time with long duration', () => {
        const wrapper = shallow(<TimeDuration millis={1000 * 60 * 60 * 24 * 178 - 1000} t={t} />);

        assert.isTrue(wrapper.is('span'));
        assert.equal(wrapper.text(), '177d 23h 59m 59s');
    });
});
