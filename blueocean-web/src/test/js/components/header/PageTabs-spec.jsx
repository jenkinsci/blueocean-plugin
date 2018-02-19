import React from 'react';
import { assert } from 'chai';
import { shallow } from 'enzyme';

import { TabLink } from '../../../../src/js/components/TabLink';
import { PageTabs } from '../../../../src/js/components/header/PageTabs';

describe("PageTabs", () => {

    it("renders empty with no children", () => {
        const wrapper = shallow(<PageTabs />);

        assert.isTrue(wrapper.is('nav'), 'must find nav element');
        assert.isTrue(!wrapper.contains('TabLink'), 'should not find TabLink elements');
    });

    it("renders two tab children", () => {
        const wrapper = shallow(
            <PageTabs>
                <TabLink to="/a">A</TabLink>
                <TabLink to="/b">B</TabLink>
            </PageTabs>
        );

        assert.isTrue(wrapper.is('nav'), 'must find nav element');
        assert.equal(wrapper.find('TabLink').length, 2, 'should find 2 TabLink elements');
    });

    it("renders one tab children when passed a falsy child", () => {
        const links = [
            <TabLink key="a" to="/a">A</TabLink>,
            false && <TabLink to="/b">B</TabLink>
        ];

        const wrapper = shallow(
            <PageTabs>
                {links}
            </PageTabs>
        );

        assert.isTrue(wrapper.is('nav'), 'must find nav element');
        assert.equal(wrapper.find('TabLink').length, 1, 'should find 1 TabLink element');
    });

});
