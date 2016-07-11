/**
 * Created by cmeyers on 7/6/16.
 */

import keymirror from 'keymirror';
import Immutable from 'immutable';
import { createSelector } from 'reselect';
import { User } from '../model/User';

/* eslint new-cap: [0] */
const { Record } = Immutable;

const FavoritesState = Record({
    user: null,
    favorites: null,
});

export const ACTION_TYPES = keymirror({
    SET_USER: null,
    SET_FAVORITES: null,
    TOGGLE_FAVORITE: null,
});

const augmentFavoritesData = (favorites) => (
    favorites.map((fav, index) => {
        const name = fav.pipeline.split('/').slice(-1).join('');
        const commitId = 'a50b3a7c7f8adc9a41b2121aea890ad7292085f6';

        fav.data = {
            name: name,
            fullName: name,
            organization: 'Jenkins',
            latestRun: {
                estimatedDurationInMillis: 60000,
                result: index % 2 === 0 ? 'SUCCESS' : 'FAILURE',
            },
            commitId: index % 2 === 0 ? commitId : commitId.substr(7, 7),
        };
        return fav;
    })
);

const actionHandlers = {
    [ACTION_TYPES.SET_USER](state, { payload }) {
        const user = new User(payload);
        return state.set('user', user);
    },
    [ACTION_TYPES.SET_FAVORITES](state, { payload }) {
        const favorites = augmentFavoritesData(payload);
        return state.set('favorites', favorites);
    },
    [ACTION_TYPES.TOGGLE_FAVORITE](state, { pipeline, favorite }) {
        // TODO: handle add and remove of pipeline from List properly
        if (pipeline) {
            return state.set('favorites', []);
        }

        return {
            pipeline,
            favorite,
        };
    },
};

const favoritesStore = state => state.favoritesStore;
export const userSelector = createSelector([favoritesStore], store => store.user);
export const favoritesSelector = createSelector([favoritesStore], store => store.favorites);

// reducer
function reducer(state = new FavoritesState(), action):FavoritesState {
    const { type } = action;
    if (type in actionHandlers) {
        return actionHandlers[type](state, action);
    }
    return state;
}

export default {
    favoritesStore: reducer,
};
