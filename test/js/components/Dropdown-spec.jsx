import 'babel-polyfill';
import React from 'react';
import { assert } from 'chai';
import { mount } from 'enzyme';

import { Dropdown } from '../../../src/js/components';

const options = ['Thor', 'Ironman', 'Captian America'];

describe("Dropdown", () => {
    it('dropDown button title should be default title', () => {
        const wrapper = mount(<Dropdown { ...{
            options,
        }}
        />);
        assert.ok(wrapper);
        const props = wrapper.find('Dropdown').find('button').props();
        assert.equal(props.title, 'Select an option');
    });
    it('dropDown button title should be the same as the first child', () => {
        const wrapper = mount(<Dropdown { ...{
            options,
            defaultOption: options[0],
        }}
        />);
        assert.ok(wrapper);
        const props = wrapper.find('Dropdown').find('button').props();
        assert.equal(props.title, options[0]);
    });
    it('dropDown button title should be the same as the title attribute', () => {
        const title = 'Works';
        const wrapper = mount(<Dropdown { ...{
            options,
            title,
            defaultOption: options[0],
        }}
        />);
        assert.ok(wrapper);
        const props = wrapper.find('Dropdown').find('button').props();
        assert.equal(props.title, title);
    });


});
