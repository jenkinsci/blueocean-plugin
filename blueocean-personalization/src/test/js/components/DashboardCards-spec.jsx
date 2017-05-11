/**
 * Created by cmeyers on 7/15/16.
 */
import React from 'react';
import { assert } from 'chai';
import { shallow } from 'enzyme';

import { DashboardCards } from '../../../main/js/components/DashboardCards';


const noLatestRun = [{
    "_class": "io.jenkins.blueocean.service.embedded.rest.FavoriteImpl",
    "_links": {
        "self": {
            "_class": "io.jenkins.blueocean.rest.hal.Link",
            "href": "/blue/rest/users/cmeyers/favorites/jdl2%252Fdocker-test/"
        }
    },
    "item": {
        "_class": "io.jenkins.blueocean.rest.impl.pipeline.BranchImpl",
        "_links": {
            "self": {
                "_class": "io.jenkins.blueocean.rest.hal.Link",
                "href": "/blue/rest/organizations/jenkins/pipelines/jdl2/branches/docker-test/"
            },
            "actions": {
                "_class": "io.jenkins.blueocean.rest.hal.Link",
                "href": "/blue/rest/organizations/jenkins/pipelines/jdl2/branches/docker-test/actions/"
            },
            "runs": {
                "_class": "io.jenkins.blueocean.rest.hal.Link",
                "href": "/blue/rest/organizations/jenkins/pipelines/jdl2/branches/docker-test/runs/"
            },
            "queue": {
                "_class": "io.jenkins.blueocean.rest.hal.Link",
                "href": "/blue/rest/organizations/jenkins/pipelines/jdl2/branches/docker-test/queue/"
            }
        },
        "actions": [],
        "displayName": "docker-test",
        "estimatedDurationInMillis": 390126,
        "fullDisplayName": "jdl2/docker-test",
        "fullName": "jdl2/docker-test",
        "name": "docker-test",
        "organization": "jenkins",
        "weatherScore": 100,
        "pullRequest": null
    }
}];

describe('DashboardCards', () => {
    let favorites;

    beforeEach(() => {
        favorites = require('../data/favorites.json');
    });

    it('renders without error for empty props', () => {
        const wrapper = shallow(
            <DashboardCards />
        );

        assert.isOk(wrapper);
    });
    it('renders without error for favorites props that has empty latestRun', () => {
        const wrapper = shallow(
            <DashboardCards favorites={noLatestRun}/>
        );

        assert.isOk(wrapper);
    });
});
