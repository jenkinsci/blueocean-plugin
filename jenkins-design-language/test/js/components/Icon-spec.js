import React from 'react';
import { assert } from 'chai';
import { shallow } from 'enzyme';

import { Icon } from '../../../src/js/components';

describe("Icon", () => {
    it("renders search icon", () => {
        const wrapper = shallow(<Icon icon="ActionSearch" />);
        assert.isTrue(wrapper.is('ActionSearch'));
    });

    it("renders null if icon name invalid", () => {
        const wrapper = shallow(<Icon icon="Garibaldi" />);
        assert.isNull(wrapper.type());
        assert.isTrue(wrapper.isEmptyRender());
    });
});
