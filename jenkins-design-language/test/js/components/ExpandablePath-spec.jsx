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

    it("renders two visible labels", () => {
        const name = 'jenkins / pipeline';
        const wrapper = shallow(<ExpandablePath path={name} />);

        assert.equal(wrapper.find('.show-label').length, 2);
    });

    it("renders three labels and two folders", () => {
        const name = 'jenkins / folder1 / folder2 / folder3 / pipeline';
        const wrapper = shallow(<ExpandablePath path={name} />);

        assert.equal(wrapper.find('.show-label').length, 3);
        assert.equal(wrapper.find('.show-folder').length, 2);
    });

    it("using hideFirst=true, renders two labels and three folders", () => {
        const name = 'jenkins / folder1 / folder2 / folder3 / pipeline';
        const wrapper = shallow(<ExpandablePath path={name} hideFirst />);

        assert.equal(wrapper.find('.show-label').length, 2);
        assert.equal(wrapper.find('.show-folder').length, 3);
    });

    it('using uriDecode=true, renders clean path name', () => {
        const name = 'jenkins / Pipeline%20Jobs / pipeline1';
        const wrapper = shallow(<ExpandablePath path={name} uriDecode />);

        const pathItems = wrapper.find('.path-item');
        assert.equal(pathItems.length, 3);
        assert.equal(pathItems.at(1).find('.path-text').text(), 'Pipeline Jobs');
    });

    it('using uriDecode=false, renders ugly path name', () => {
        const name = 'jenkins / Pipeline%20Jobs / pipeline1';
        const wrapper = shallow(<ExpandablePath path={name} uriDecode={false} />);

        const pathItems = wrapper.find('.path-item');
        assert.equal(pathItems.length, 3);
        assert.equal(pathItems.at(1).find('.path-text').text(), 'Pipeline%20Jobs');
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
