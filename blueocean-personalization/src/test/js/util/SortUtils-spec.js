/**
 * Created by cmeyers on 9/15/16.
 */
import { assert } from 'chai';

import { extractStatus as status, sortByStatusByRecent } from '../../../main/js/util/SortUtils';

const org = (favorite) => favorite.item.organization;
const fullName = (favorite) => favorite.item.fullName;

describe('SortUtils', () => {
    let favorites;

    beforeEach(() => {
        favorites = require('../data/favorites.json');
    });

    describe('sortByStatusByRecent', () => {
        it('sorts the cards by status then by most recent', () => {
            const sortedFavorites = favorites.sort(sortByStatusByRecent);

            assert.equal(sortedFavorites.length, 12);
            assert.equal(status(sortedFavorites[0]), 'UNKNOWN');
            assert.equal(status(sortedFavorites[1]), 'FAILURE');
            assert.equal(status(sortedFavorites[2]), 'ABORTED');
            assert.equal(status(sortedFavorites[3]), 'NOT_BUILT');
            assert.equal(status(sortedFavorites[4]), 'UNSTABLE');

            assert.equal(status(sortedFavorites[5]), 'RUNNING');
            assert.equal(org(sortedFavorites[5]), 'jenkins');
            assert.equal(fullName(sortedFavorites[5]), 'jdl2/docker-test');

            assert.equal(status(sortedFavorites[6]), 'RUNNING');
            assert.equal(org(sortedFavorites[6]), 'jenkins');
            assert.equal(fullName(sortedFavorites[6]), 'jdl1/docker-test');

            assert.equal(status(sortedFavorites[7]), 'QUEUED');
            assert.equal(org(sortedFavorites[7]), 'jenkins');
            assert.equal(fullName(sortedFavorites[7]), 'test5/master');

            assert.equal(status(sortedFavorites[8]), 'QUEUED');
            assert.equal(org(sortedFavorites[8]), 'jenkins');
            assert.equal(fullName(sortedFavorites[8]), 'test6/master');

            assert.equal(status(sortedFavorites[9]), 'SUCCESS');
            assert.equal(org(sortedFavorites[9]), 'jankins');
            assert.equal(fullName(sortedFavorites[9]), 'jdl1/docker-test2');

            assert.equal(status(sortedFavorites[10]), 'SUCCESS');
            assert.equal(org(sortedFavorites[10]), 'jenkins');
            assert.equal(fullName(sortedFavorites[10]), 'jenkinsfile-experiments/test-branch-1');

            assert.equal(status(sortedFavorites[11]), 'SUCCESS');
            assert.equal(org(sortedFavorites[11]), 'jenkins');
            assert.equal(fullName(sortedFavorites[11]), 'jenkinsfile-experiments/master');
        });
    });
});
