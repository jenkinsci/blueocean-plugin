/**
 * Created by cmeyers on 7/15/16.
 */
import React from 'react';
import { assert } from 'chai';
import { shallow } from 'enzyme';
import Immutable from 'immutable';

import { User } from '../../../main/js/model/User';
import { DashboardCards } from '../../../main/js/components/DashboardCards';
import { CapabilityTestUtils } from '../CapabilityTestUtils';

const { List } = Immutable;

const user = new User({
    id: 'cmeyers',
});

describe('DashboardCards', () => {
    let favorites;
    let testUtils;

    beforeEach((done) => {
        favorites = require('../data/favorites.json');

        testUtils = new CapabilityTestUtils();
        testUtils.bindCapability(
            'io.jenkins.blueocean.rest.impl.pipeline.BranchImpl',
            'io.jenkins.blueocean.rest.model.BlueBranch'
        );
        testUtils.augment(favorites);

        done();
    });

    afterEach(() => {
        testUtils.unbindAll();
    });

    it('renders without error for empty props', () => {
        const wrapper = shallow(
            <DashboardCards />
        );

        assert.isOk(wrapper);
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
