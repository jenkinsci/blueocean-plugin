import React from 'react';
import { assert } from 'chai';
import { shallow } from 'enzyme';

import LinkifiedText from '../../../main/js/components/LinkifiedText';

describe("LinkifiedText", () => {

    it("renders text with no link", () => {
        const wrapper = shallow(<LinkifiedText text="123" />);

        assert.isTrue(wrapper.is('span'));
        assert.equal(wrapper.text(), '123');
    });

    it("renders linked text with no issues", () => {
        const wrapper = shallow(<LinkifiedText text="123" textLink="link" />);

        assert.isTrue(wrapper.is('Link'));
        assert.equal(wrapper.prop('to'), 'link');
    });

    it("renders text with no linked issues in text", () => {
        const mockIssues = [
            {
                'id': 'JIRA-XXXX',
                'url': 'https://issue.url'
            }
        ];
        const wrapper = shallow(<LinkifiedText text="123" issues={mockIssues} />);

        assert.isTrue(wrapper.is('span'));
        assert.equal(wrapper.text(), '123');
    });

    it("renders text with linked issues in text", () => {
        const mockIssues = [
            {
                'id': 'JIRA-XXXX',
                'url': 'https://issue.url'
            }
        ];
        const wrapper = shallow(<LinkifiedText text="test JIRA-XXXX text" issues={mockIssues} />);

        assert.equal(wrapper.find('a').text(), 'JIRA-XXXX');
        assert.equal(wrapper.find('a').prop('href'), 'https://issue.url');
    });

    it("renders linked text with linked issues in text", () => {
        const mockIssues = [
            {
                'id': 'JIRA-XXXX',
                'url': 'https://issue.url'
            }
        ];
        const wrapper = shallow(<LinkifiedText text="test JIRA-XXXX text" textLink="link" issues={mockIssues} />);

        assert.equal(wrapper.find('a').text(), 'JIRA-XXXX');
        assert.equal(wrapper.find('a').prop('href'), 'https://issue.url');
        assert.equal(wrapper.find('Link').first().prop('to'), 'link');
    });
});
