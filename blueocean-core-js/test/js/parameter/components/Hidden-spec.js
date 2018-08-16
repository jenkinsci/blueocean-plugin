import React from 'react';
import { assert } from 'chai';
import { shallow } from 'enzyme';

import { Hidden } from '../../../../src/js/parameter/components/Hidden';


describe('Hidden Param Component', () => {
    const mockDefaultParameterValue = {
        name: 'hiddenParam',
        value: ''
    };

    it("Test cleaning of tags in name", () => {
        const wrapper = shallow(<Hidden defaultParameterValue={mockDefaultParameterValue} name="<span>testName</span>" />);
        const inputElement = wrapper.find('input')
        assert.equal(inputElement.prop('type'), 'hidden');
        assert.equal(inputElement.prop('name'), 'testName');
        assert.equal(inputElement.prop('value'), '');
    });
});
