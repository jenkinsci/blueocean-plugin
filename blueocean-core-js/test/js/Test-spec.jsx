/**
 * Created by cmeyers on 7/29/16.
 */
import React from 'react';
import { assert } from 'chai';
import { shallow } from 'enzyme';

import { Test } from '../../src/js/Test';

describe("Test", () => {
    it("renders", () => {
        const wrapper = shallow(<Test />);

        assert.isTrue(wrapper.equals(
            <div>Test</div>
        ));
    });
});
