import React from 'react';
import { assert } from 'chai';
import { shallow } from 'enzyme';

import { CommitId } from '../../../src/js/components';

describe("CommitId", () => {

    it("renders dash with no data", () => {
        const wrapper = shallow(<CommitId />);
        assert.isTrue(wrapper.is('code'));
        assert.equal(wrapper.text(), '–');
    });

    it("renders if does not match git sha-1 regexp", () => {
        const wrapper = shallow(<CommitId commitId="123" />);
        assert.isTrue(wrapper.is('code'));
        assert.equal(wrapper.text(), '123');
    });

    it("renders git sha-1 with proper length", () => {
        const wrapper = shallow(<CommitId commitId="676b757ecb542a44dd6f63fd1fb08b659f7a7b03" />);
        assert.isTrue(wrapper.is('code'));
        assert.equal(wrapper.text().length, 7);
        assert.equal(wrapper.text(), '676b757');
    });

    it("renders link when url prop is passed along", () => {
        const wrapper = shallow(<CommitId commitId="123" url="link" />);
        assert.isTrue(wrapper.is('a[href="link"]'));
    });
});
