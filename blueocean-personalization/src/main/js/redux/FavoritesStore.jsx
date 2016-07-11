/**
 * Created by cmeyers on 7/6/16.
 */

import keymirror from 'keymirror';
import Immutable from 'immutable';
import { createSelector } from 'reselect';
import { User } from '../model/User';

/* eslint new-cap: [0] */
const { Record, List } = Immutable;

export const FavoritesState = Record({
    user: null,
    favorites: null,
});

export const ACTION_TYPES = keymirror({
    SET_USER: null,
    SET_FAVORITES: null,
    TOGGLE_FAVORITE: null,
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
    [ACTION_TYPES.TOGGLE_FAVORITE](state, { addFavorite, branchToRemove, payload }) {
        const favoritesList = state.get('favorites');

        if (addFavorite) {
            const appendedList = favoritesList.push(payload);
            return state.set('favorites', appendedList);
        }

        const toggledBranchHref = branchToRemove._links.self.href;
        const prunedList = favoritesList.filter(fav => {
            const favoritedBranch = fav.item;
            return favoritedBranch._links.self.href !== toggledBranchHref;
        });

        return state.set('favorites', prunedList);
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
