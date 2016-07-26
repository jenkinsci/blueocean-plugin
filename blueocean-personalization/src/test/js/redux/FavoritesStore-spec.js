/**
 * Created by cmeyers on 7/11/16.
 */
import { assert } from 'chai';

import Store from '../../../main/js/redux/FavoritesStore';
import { ACTION_TYPES, FavoritesState } from '../../../main/js/redux/FavoritesStore';

const getDefaultFavorites = () =>
    JSON.parse(`
        [
            {
                "_class": "io.jenkins.blueocean.service.embedded.rest.FavoriteImpl",
                "_links": {
                    "self": {
                        "_class": "io.jenkins.blueocean.rest.hal.Link",
                        "href": "/blue/rest/users/cmeyers/favorites/blueocean%2FUX-301/"
                    }
                },
                "item": {
                    "_class": "io.jenkins.blueocean.rest.impl.pipeline.BranchImpl",
                    "_links": {
                        "self": {
                            "_class": "io.jenkins.blueocean.rest.hal.Link",
                            "href": "/blue/rest/organizations/jenkins/pipelines/blueocean/branches/UX-301/"
                        }
                    }
                }
            },
            {
                "_class": "io.jenkins.blueocean.service.embedded.rest.FavoriteImpl",
                "_links": {
                    "self": {
                        "_class": "io.jenkins.blueocean.rest.hal.Link",
                        "href": "/blue/rest/users/cmeyers/favorites/jenkinsfile-experiments%2Fmaster/"
                    }
                },
                "item": {
                    "_class": "io.jenkins.blueocean.rest.impl.pipeline.BranchImpl",
                    "_links": {
                        "self": {
                            "_class": "io.jenkins.blueocean.rest.hal.Link",
                            "href": "/blue/rest/organizations/jenkins/pipelines/jenkinsfile-experiments/branches/master/"
                        }
                    }
                }
            }
        ]
    `);

const createBranch = (selfHref) => {
    return {
        _links: {
            self: {
                href: selfHref,
            },
        },
    };
};

describe('favoritesStore', () => {
    const stateWithFavorites = Store.favoritesStore(
        new FavoritesState(),
        {
            type: ACTION_TYPES.SET_FAVORITES,
            payload: getDefaultFavorites(),
        }
    );

    describe('SET_FAVORITES', () => {
        it('stores the favorites correctly', () => {
            assert.equal(stateWithFavorites.get('favorites').size, 2);
        });
    });

    describe('TOGGLE_FAVORITE', () => {
        it('removes an existing favorite when toggled', () => {
            const newState = Store.favoritesStore(
                stateWithFavorites,
                {
                    type: ACTION_TYPES.TOGGLE_FAVORITE,
                    addFavorite: false,
                    branch: createBranch(
                        '/blue/rest/organizations/jenkins/pipelines/blueocean/branches/UX-301/'
                    ),
                    payload: null,
                }
            );

            const favorites = newState.get('favorites');
            assert.isOk(favorites);
            assert.equal(favorites.size, 1);

            // ensure favorite is not present in list
            for (const fav of favorites.values()) {
                assert.notEqual(
                    '/blue/rest/organizations/jenkins/pipelines/blueocean/branches/UX-301/',
                    fav.item._links.self.href
                );
            }
        });

        it('adds a new favorite when toggled', () => {
            const selfHref = '/blue/rest/organizations/jenkins/pipelines/test1/branches/master/';
            const favoriteToAdd = {
                item: {
                    _links: {
                        self: {
                            href: selfHref,
                        },
                    },
                },
            };

            const newState = Store.favoritesStore(
                stateWithFavorites,
                {
                    type: ACTION_TYPES.TOGGLE_FAVORITE,
                    addFavorite: true,
                    branch: null,
                    payload: favoriteToAdd,
                }
            );

            const favorites = newState.get('favorites');
            assert.isOk(favorites);
            assert.equal(favorites.size, 3);
            assert.isOk(favorites.includes(favoriteToAdd));
        });
    });
});
