/**
 * Created by cmeyers on 7/15/16.
 */
import React from 'react';
import { assert } from 'chai';
import { shallow } from 'enzyme';
import Immutable from 'immutable';

import { User } from '../../../main/js/model/User';
import { DashboardCards } from '../../../main/js/components/DashboardCards';
import { bindCapability } from '../MetadataUtils';

const { List } = Immutable;

const user = new User({
    id: 'cmeyers',
});

const favorites = require('../data/favorites.json');
const favorlitesList = new List(favorites);

describe('DashboardCards', () => {
    beforeEach(() => {
        // needed to prevent DashboardCards from blowing up internally
        bindCapability(
            'io.jenkins.blueocean.rest.impl.pipeline.BranchImpl',
            'io.jenkins.blueocean.rest.model.BlueBranch'
        );
    });

    it('renders without error for empty props', () => {
        const wrapper = shallow(
            <DashboardCards />
        );

        assert.isOk(wrapper);
    });

    it('sorts the cards by status, then by name', () => {
        const wrapper = shallow(
            <DashboardCards
              user={user}
              favorites={favorlitesList}
            />
        );

        const cards = wrapper.find('PipelineCard');

        assert.equal(cards.length, 12);
        assert.equal(cards.at(0).props().status, 'UNKNOWN');
        assert.equal(cards.at(1).props().status, 'FAILURE');
        assert.equal(cards.at(2).props().status, 'ABORTED');
        assert.equal(cards.at(3).props().status, 'NOT_BUILT');
        assert.equal(cards.at(4).props().status, 'UNSTABLE');

        assert.equal(cards.at(5).props().status, 'RUNNING');
        assert.equal(cards.at(5).props().organization, 'jenkins');
        assert.equal(cards.at(5).props().pipeline, 'jdl2');
        assert.equal(cards.at(5).props().branch, 'docker-test');

        assert.equal(cards.at(6).props().status, 'RUNNING');
        assert.equal(cards.at(6).props().organization, 'jenkins');
        assert.equal(cards.at(6).props().pipeline, 'jdl1');
        assert.equal(cards.at(6).props().branch, 'docker-test');

        assert.equal(cards.at(7).props().status, 'QUEUED');
        assert.equal(cards.at(7).props().organization, 'jenkins');
        assert.equal(cards.at(7).props().pipeline, 'test5');
        assert.equal(cards.at(7).props().branch, 'master');

        assert.equal(cards.at(8).props().status, 'QUEUED');
        assert.equal(cards.at(8).props().organization, 'jenkins');
        assert.equal(cards.at(8).props().pipeline, 'test6');
        assert.equal(cards.at(8).props().branch, 'master');

        assert.equal(cards.at(9).props().status, 'SUCCESS');
        assert.equal(cards.at(9).props().organization, 'jankins');
        assert.equal(cards.at(9).props().pipeline, 'jdl1');
        assert.equal(cards.at(9).props().branch, 'docker-test');

        assert.equal(cards.at(10).props().status, 'SUCCESS');
        assert.equal(cards.at(10).props().organization, 'jenkins');
        assert.equal(cards.at(10).props().pipeline, 'jenkinsfile-experiments');
        assert.equal(cards.at(10).props().branch, 'test-branch-1');

        assert.equal(cards.at(11).props().status, 'SUCCESS');
        assert.equal(cards.at(11).props().organization, 'jenkins');
        assert.equal(cards.at(11).props().pipeline, 'jenkinsfile-experiments');
        assert.equal(cards.at(11).props().branch, 'master');
    });

    it('forces a favorite without latestRun to NOT_BUILT', () => {
        const unbuiltFavorite = new List([
            {
                _class: 'io.jenkins.blueocean.service.embedded.rest.FavoriteImpl',
                _links: {
                    self: {
                        _class: 'io.jenkins.blueocean.rest.hal.Link',
                        href: '/blue/rest/users/cmeyers/favorites/blueocean%252FUX-301/',
                    },
                },
                item: {
                    _class: 'io.jenkins.blueocean.rest.impl.pipeline.BranchImpl',
                    displayName: 'UX-301',
                    estimatedDurationInMillis: 73248,
                    fullName: 'blueocean/UX-301',
                    lastSuccessfulRun: null,
                    latestRun: null,
                    name: 'UX-301',
                    organization: 'jenkins',
                    weatherScore: 0,
                    pullRequest: null,
                },
            },
        ]);

        const wrapper = shallow(
            <DashboardCards
              user={user}
              favorites={unbuiltFavorite}
            />
        );

        const cards = wrapper.find('PipelineCard');

        assert.equal(cards.length, 1);
        assert.equal(cards.at(0).props().status, 'NOT_BUILT');
    });
});
