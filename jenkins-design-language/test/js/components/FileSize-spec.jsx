import React from 'react';
import { assert } from 'chai';
import { shallow } from 'enzyme';

import { FileSize } from '../../../src/js/components';

describe("FileSize", () => {

    it("renders dash with no data", () => {
        const wrapper = shallow(<FileSize />);

        assert.isTrue(wrapper.equals(
            <span>-</span>
        ));
    });

    it("renders dash with invalid data", () => {
        const wrapper = shallow(<FileSize date="invalid data" />);

        assert.isTrue(wrapper.equals(
            <span>-</span>
        ));
    });

    it("renders 0 bytes", () => {
        const wrapper = shallow(<FileSize bytes={0} />);

        assert.isTrue(wrapper.is('span'));
        assert.equal(wrapper.text(), '0 bytes');
    });

    it("renders 4 bytes", () => {
        const wrapper = shallow(<FileSize bytes={4} />);

        assert.isTrue(wrapper.is('span'));
        assert.equal(wrapper.text(), '4 bytes');
    });

    it("renders 1.5 MB", () => {
        const wrapper = shallow(<FileSize bytes={1024*1024*1.45} />);

        assert.isTrue(wrapper.is('span'));
        assert.equal(wrapper.text(), '1.5 MB');
    });

    it("renders very large values reasonably", () => {
        const wrapper = shallow(<FileSize bytes={Math.pow(1024, 7)} />);

        assert.isTrue(wrapper.is('span'));
        assert.equal(wrapper.text(), '1048576 PB');
    });

});
