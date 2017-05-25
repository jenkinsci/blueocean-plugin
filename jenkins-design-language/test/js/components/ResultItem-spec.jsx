import React from 'react';
import { assert } from 'chai';
import { shallow } from 'enzyme';

import { ResultItem } from '../../../src/js/components';

describe("ResultItem", () => {

    it("visible by default", () => {
        const wrapper = shallow(<ResultItem result="success" label="Expanded by default step"
            extraInfo="13 sec" data="foxtrot" expanded="true">Should be visible by default</ResultItem>);

        assert.isTrue(wrapper.contains("Should be visible by default"));
    });

    it("hidden by default", () => {
        const wrapper = shallow(<ResultItem result="success" label="Expanded by default step"
            extraInfo="13 sec" data="foxtrot" expanded="false">Shouldn't be visible</ResultItem>);

        assert.isFalse(wrapper.contains("Should be visible by default"));
    });

});
