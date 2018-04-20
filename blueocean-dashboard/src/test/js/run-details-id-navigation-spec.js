import React from 'react';
import { assert } from 'chai';
import { shallow } from 'enzyme';

import { RunIdNavigation } from '../../main/js/components/RunDetailsHeader';

import { i18nTranslator } from '@jenkins-cd/blueocean-core-js';
const t = i18nTranslator('blueocean-dashboard');

const mockRuns = [
    {
        'id': '1',
        'organization': 'jenkins',
        'pipeline': 'mockPipeline',
        '_links': {
            'nextRun': {
                '_class': 'io.jenkins.blueocean.rest.hal.Link',
                'href': '/blue/rest/organizations/jenkins/pipelines/mockPipeline/runs/2/'
            },
        }
    },
    {
        'id': '2',
        'organization': 'jenkins',
        'pipeline': 'mockPipeline',
        '_links': {
            'prevRun': {
                '_class': 'io.jenkins.blueocean.rest.hal.Link',
                'href': '/blue/rest/organizations/jenkins/pipelines/mockPipeline/runs/1/'
            },
            'nextRun': {
                '_class': 'io.jenkins.blueocean.rest.hal.Link',
                'href': '/blue/rest/organizations/jenkins/pipelines/mockPipeline/runs/3/'
            },
        }
    },
    {
        'id': '3',
        'organization': 'jenkins',
        'pipeline': 'mockPipeline',
        '_links': {
            'prevRun': {
                '_class': 'io.jenkins.blueocean.rest.hal.Link',
                'href': '/blue/rest/organizations/jenkins/pipelines/mockPipeline/runs/2/'
            },
        }
    }
];

const mockPipeline = {
    'organization': 'jenkins',
    'fullName': 'mockPipeline',
}

import { mockExtensionsForI18n } from './mock-extensions-i18n';
mockExtensionsForI18n();

describe('RunDetailsIdNavigation', () => {
    beforeAll(() => mockExtensionsForI18n());

    it('check next link on run with id = 1', () => {
        let wrapper = shallow(<RunIdNavigation run={mockRuns[0]} pipeline={mockPipeline} branchName='branchName' t={t} />);

        //check that there is only one link for first run (only next)
        assert.equal(wrapper.find('Link').length, 1);

        //check next link
        assert.equal(wrapper.find('Link').first().props().title, 'Next Run');
        assert.equal(wrapper.find('Link').first().props().to, '/organizations/jenkins/mockPipeline/detail/branchName/2/pipeline');
    });


    it('check next/prev links on run with id = 2', () => {
        let wrapper = shallow(<RunIdNavigation run={mockRuns[1]} pipeline={mockPipeline} branchName='branchName' t={t} />);

        //check prev link
        assert.equal(wrapper.find('Link').first().props().title, 'Previous Run');
        assert.equal(wrapper.find('Link').first().props().to, '/organizations/jenkins/mockPipeline/detail/branchName/1/pipeline');

        //check next link
        assert.equal(wrapper.find('Link').at(1).props().title, 'Next Run');
        assert.equal(wrapper.find('Link').at(1).props().to, '/organizations/jenkins/mockPipeline/detail/branchName/3/pipeline');
    });

    it('check next link on run with id = 3', () => {
        let wrapper = shallow(<RunIdNavigation run={mockRuns[2]} pipeline={mockPipeline} branchName='branchName' t={t} />);

        //check that there is only one link for the last run (only prev)
        assert.equal(wrapper.find('Link').length, 1);

        //check prev link
        assert.equal(wrapper.find('Link').first().props().title, 'Previous Run');
        assert.equal(wrapper.find('Link').first().props().to, '/organizations/jenkins/mockPipeline/detail/branchName/2/pipeline');
    });
});
