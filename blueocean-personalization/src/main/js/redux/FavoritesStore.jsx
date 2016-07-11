/**
 * Created by cmeyers on 7/6/16.
 */

import keymirror from 'keymirror';
import Immutable from 'immutable';
import { createSelector } from 'reselect';
import { User } from '../model/User';

/* eslint new-cap: [0] */
const { Record, List } = Immutable;

const FavoritesState = Record({
    user: null,
    favorites: null,
});

export const ACTION_TYPES = keymirror({
    SET_USER: null,
    SET_FAVORITES: null,
});

const actionHandlers = {
    [ACTION_TYPES.SET_USER](state, { payload }) {
        const user = new User(payload);
        return state.set('user', user);
    },
    [ACTION_TYPES.SET_FAVORITES](state, { payload }) {
        const favoriteList = new List(payload);
        return state.set('favorites', favoriteList);
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
