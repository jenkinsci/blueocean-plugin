import React from 'react';
import { assert } from 'chai';
import { shallow, mount } from 'enzyme';
import { ClassicCreationLink as Link }  from '../../../main/js/creation/ClassicCreationLink';
import { mockExtensionsForI18n } from '../mock-extensions-i18n';
import { prepareMount } from '../util/EnzymeUtils';
import { AppConfig } from '@jenkins-cd/blueocean-core-js';

const context = { params: {}};
describe('ClassicCreationLink', () => {
    beforeAll(() => {
        mockExtensionsForI18n();
        prepareMount();
    });

    it('returns the root create link when no organization is specified', () => {
        const wrapper = shallow(<Link />, { context });
        assert.ok(wrapper);
        assert.equal(wrapper.find('a').length, 1);
        assert.equal(wrapper.find('a').props().href, '/jenkins/newJob'); // make sure it will lead to create page in classic jenkins
    });

    it('returns the folder based create link when organization', () => {
        AppConfig.getOrganizationGroup = () => "/accounting";
        const wrapper = mount(<Link />, { context });
        assert.ok(wrapper);
        assert.equal(wrapper.find('a').length, 1);
        assert.equal(wrapper.find('a').props().href, '/jenkins/job/accounting/newJob'); // make sure it will lead to create page in classic jenkins but in the organization folder
    });
});
