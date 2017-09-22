import React from 'react';
import { assert } from 'chai';
import { shallow, render } from 'enzyme';

import { ResultItem } from '../../../src/js/components';

describe('ResultItem', () => {

    describe('expanded prop', () => {
        it('is visible when expanded=true', () => {
            const wrapper = shallow(<ResultItem result="success" label="Label" expanded={true}>Contents</ResultItem>);

            assert.isTrue(wrapper.contains('Contents'));
        });

        it('is hidden when expanded=false', () => {
            const wrapper = shallow(<ResultItem result="success" label="Label" expanded={false}>Contents</ResultItem>);

            assert.isFalse(wrapper.contains('Contents'));
        });

        it('is hidden by default', () => {
            const wrapper = shallow(<ResultItem result="success" label="Label">Contents</ResultItem>);

            assert.isFalse(wrapper.contains('Contents'));
        });
    });

    describe('url handling', () => {

        const component = (
            // <ResultItem result="success" label="Foo bar baz"/>
            <ResultItem result="success" label="Foo http://example.org/deezNuts/ bar example.com baz" />
        );

        it('converts prefixed urls in the label to links', () => {
            const wrapper = render(component);

            const html = wrapper.html();

            const expectedURL = /<a.*?>http:\/\/example.org\/deezNuts\/<\/a>/;

            assert.isTrue(expectedURL.test(html), 'should contain an link for http://example.org/deezNuts/');
            assert.isFalse(html.indexOf('bar example.com baz') === -1, 'example.com should not become a link');
        });
    });

});
