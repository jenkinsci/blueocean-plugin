/**
 * Created by cmeyers on 8/22/16.
 */
import 'babel-polyfill';
import React from 'react';
import { assert } from 'chai';
import { render, shallow } from 'enzyme';

import { Toaster } from '../../../src/js/components';

describe("Toaster", () => {

    it("renders empty container with no data", () => {
        const wrapper = shallow(<Toaster />);

        assert.equal(
            wrapper.html(),
            `<div class="toaster"><span></span></div>`
        );
    });

    it("renders a single toast", () => {
        const _toasts = [
            { id: 1, text: "Hello World", action: "CLOSE" },
        ];

        const toaster = render(
            <Toaster toasts={_toasts} />
        );

        const toasts = toaster.find('.toast');
        assert.equal(toasts.length, 1);

        assert.equal(toasts.find('.text').length, 1);
        assert.equal(toasts.find('.action').length, 1);
        assert.equal(toasts.find('.dismiss').length, 1);
    });

});
