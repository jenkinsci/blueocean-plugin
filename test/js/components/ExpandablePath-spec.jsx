/**
 * Created by cmeyers on 10/4/16.
 */
import React from 'react';
import { assert } from 'chai';
import { shallow } from 'enzyme';

import { ExpandablePath } from '../../../src/js/components';

describe("ExpandablePath", () => {

    it("renders nothing with no data", () => {
        const wrapper = shallow(<ExpandablePath />);

        assert.isTrue(wrapper.equals(null));
    });

    it("by default, renders two visible labels", () => {
        const name = 'jenkins / pipeline';
        const wrapper = shallow(<ExpandablePath path={name} />);

        assert.equal(wrapper.find('.show-label').length, 2);
    });

    it("by default, renders three labels and two folders", () => {
        const name = 'jenkins / folder1 / folder2 / folder3 / pipeline';
        const wrapper = shallow(<ExpandablePath path={name} />);

        assert.equal(wrapper.find('.show-label').length, 3);
        assert.equal(wrapper.find('.show-folder').length, 2);
    });

    it("using hideFirst, renders two labels and three folders", () => {
        const name = 'jenkins / folder1 / folder2 / folder3 / pipeline';
        const wrapper = shallow(<ExpandablePath path={name} hideFirst />);

        assert.equal(wrapper.find('.show-label').length, 2);
        assert.equal(wrapper.find('.show-folder').length, 3);
    });

    describe('replaceLastPathElement', () => {

        it('replaces the last path element', () => {
            const path = 'Jenkins / folder1 / folder2 / pipeline';
            const element = 'Fancy Pipeline Name';
            const result = ExpandablePath.replaceLastPathElement(path, element);
            assert.equal(result.split('/').slice(-1), element);
        });

    });

});
