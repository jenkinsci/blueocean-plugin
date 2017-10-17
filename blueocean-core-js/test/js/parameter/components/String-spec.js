import React from 'react';
import { assert } from 'chai';
import { shallow } from 'enzyme';

import { String } from '../../../../src/js/parameter/components/String';


describe('String Param Component', () => {
    const mockDefaultParameterValue = {
        name: 'testName',
        value: 'testValue'
    };

    it("Test cleaning of tags in description and name", () => {
        const wrapper = shallow(<String defaultParameterValue={mockDefaultParameterValue} description="<span>desc</span>" name="<span>testName</span>" />);
        assert.isTrue(wrapper.is('FormElement'));
        assert.equal(wrapper.prop('title'), 'desc');
        assert.equal(wrapper.find('TextInput').prop('name'), 'testName');
    });

    it("Test title with empty description", () => {
        const wrapper = shallow(<String defaultParameterValue={mockDefaultParameterValue} description="" name="testName" />);

        assert.isTrue(wrapper.is('FormElement'));
        assert.equal(wrapper.prop('title'), 'testName');
    });
});
