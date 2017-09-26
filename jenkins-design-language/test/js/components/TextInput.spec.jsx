import React from 'react';
import { assert } from 'chai';
import { mount } from 'enzyme';

import { TextInput } from '../../../src/js/components';


describe("TextInput", () => {
    it('TextInput should render ok but with undefined props.', () => {
        const wrapper = mount(<TextInput />);
        assert.ok(wrapper);
        assert.equal(wrapper.find('input').props().placeholder, undefined);
    });
    it('TextInput should render ok with placeholder.', () => {
        const wrapper = mount(<TextInput placeholder="placeholder"/>);
        assert.ok(wrapper);
        assert.equal(wrapper.find('input').props().placeholder, "placeholder");
    });
    it('TextInput should render aria props.', () => {
        const wrapper = mount(<TextInput ariaLabel="placeholder"/>);
        assert.ok(wrapper);
        assert.equal(wrapper.find('input').props()['aria-label'], "placeholder");
    });
    it('TextInput should render aria props with placeholder value as fallback.', () => {
        const wrapper = mount(<TextInput placeholder="placeholder"/>);
        assert.ok(wrapper);
        assert.equal(wrapper.find('input').props()['aria-label'], "placeholder");
    });
});
