import 'babel-polyfill';
import React from 'react';
import { assert } from 'chai';
import { mount } from 'enzyme';
import '../utils/rAf';

import { Dropdown } from '../../../src/js/components';

const options = ['Thor', 'Ironman', 'Captain America'];

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
    it('dropDown should show footer on click and our options', () => {
        const wrapper = mount(<Dropdown { ...{
            options,
            footer: <div id="unit">This is a custom footer</div>,
        }}
        />);
        assert.ok(wrapper);
        const drop = wrapper.find('Dropdown');
        const props = drop.find('button').props();
        assert.equal(props.title, 'Select an option');
        // click on dropDown button
        drop.find('button').simulate('click');
        assert.equal(drop.find('ul.Dropdown-menu').length, 1);
        assert.equal(drop.find('ul.Dropdown-menu li').length, options.length); // dropdown options same length then our array?
        assert.equal(drop.find('#unit').length, 1); // only on footer?
    });
    it('dropDown should still have focus after option selection', () => {
        const wrapper = mount(<Dropdown { ...{
            options,
        }}
        />);
        assert.ok(wrapper);
        const drop = wrapper.find('Dropdown');
        // click the dropDown button
        drop.find('button').simulate('click');
        // click on the 1st option
        drop.find("li a").first().simulate('click');
        //get focused element
        const focusedElement = document.activeElement;
        //verify that the focused element is the dropdown button
        assert.equal(focusedElement, drop.find('button').get(0));
    });
});
