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
        const mockPartialTextLinks = [
            {
                'id': 'JIRA-XXXX',
                'url': 'https://issue.url'
            }
        ];
        const wrapper = shallow(<LinkifiedText text="123" partialTextLinks={mockPartialTextLinks} />);

        assert.isTrue(wrapper.is('span'));
        assert.equal(wrapper.text(), '123');
    });

    it("renders text with linked issues in text", () => {
        const mockPartialTextLinks = [
            {
                'id': 'JIRA-XXXX',
                'url': 'https://issue.url'
            }
        ];
        const wrapper = shallow(<LinkifiedText text="test JIRA-XXXX text" partialTextLinks={mockPartialTextLinks} />);

        assert.equal(wrapper.find('a').text(), 'JIRA-XXXX');
        assert.equal(wrapper.find('a').prop('href'), 'https://issue.url');
    });

    it("renders linked text with linked issues in text", () => {
        const mockPartialTextLinks = [
            {
                'id': 'JIRA-XXXX',
                'url': 'https://issue.url'
            }
        ];
        const wrapper = shallow(<LinkifiedText text="test JIRA-XXXX text" textLink="link" partialTextLinks={mockPartialTextLinks} />);

        assert.equal(wrapper.find('a').text(), 'JIRA-XXXX');
        assert.equal(wrapper.find('a').prop('href'), 'https://issue.url');
        assert.equal(wrapper.find('Link').first().prop('to'), 'link');
    });

    it("renders linked text with 2 linked issues in text", () => {
        const mockPartialTextLinks = [
            {
                'id': 'JIRA-XXXX',
                'url': 'https://issue.url'
            },
            {
                'id': 'Link-XXXX',
                'url': 'https://link.url'
            }
        ];
        const wrapper = shallow(<LinkifiedText text="test JIRA-XXXX text Link-XXXX test" textLink="link" partialTextLinks={mockPartialTextLinks} />);

        assert.equal(wrapper.find('a').first().text(), 'JIRA-XXXX');
        assert.equal(wrapper.find('a').first().prop('href'), 'https://issue.url');

        assert.equal(wrapper.find('a').at(1).text(), 'Link-XXXX');
        assert.equal(wrapper.find('a').at(1).prop('href'), 'https://link.url');

        assert.equal(wrapper.find('Link').first().prop('to'), 'link');
    });
});
