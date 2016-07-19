/**
 * Created by cmeyers on 7/15/16.
 */
import React from 'react';
import { assert } from 'chai';
import { shallow } from 'enzyme';
import Immutable from 'immutable';

import { User } from '../../../main/js/model/User';
import { favorites } from '../data/favorites';
import { DashboardCards } from '../../../main/js/components/DashboardCards';

const { List } = Immutable;

const user = new User({
    id: 'cmeyers',
});

const favorlitesList = new List(favorites);

describe('DashboardCards', () => {
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
});
