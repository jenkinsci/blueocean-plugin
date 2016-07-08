/**
 * Created by cmeyers on 7/6/16.
 */

import keymirror from 'keymirror';
import Immutable from 'immutable';
import { createSelector } from 'reselect';

/* eslint new-cap: [0] */
const { Record } = Immutable;

const FavoritesState = Record({
    favorites: null,
});

export const ACTION_TYPES = keymirror({
    SET_FAVORITES: null,
});

const actionHandlers = {
    [ACTION_TYPES.SET_FAVORITES](state, { payload }) {
        return state.set('favorites', payload);
    },
};

const favoritesStore = state => state.favoritesStore;
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
