/**
 * Created by cmeyers on 7/6/16.
 */

import keymirror from 'keymirror';
import Immutable from 'immutable';
import { createSelector } from 'reselect';

import { FavoritesSortHelper } from '../util/SortUtils';
import { checkMatchingFavoriteUrls } from '../util/FavoriteUtils';

/* eslint new-cap: [0] */
const { Record, List } = Immutable;

const sortHelper = new FavoritesSortHelper();

export const FavoritesState = Record({
    favorites: null,
});

export const ACTION_TYPES = keymirror({
    SET_FAVORITES: null,
    SORT_FAVORITES: null,
    TOGGLE_FAVORITE: null,
    UPDATE_RUN: null,
});

function clone(json) {
    return JSON.parse(JSON.stringify(json));
}

const actionHandlers = {
    [ACTION_TYPES.SET_FAVORITES](state, { payload }) {
        const favoriteList = new List(payload);
        const sortedList = sortHelper.applyStandardSort(favoriteList);
        return state.set('favorites', sortedList);
    },
    [ACTION_TYPES.SORT_FAVORITES](state) {
        const favoriteList = state.get('favorites');

        if (favoriteList && favoriteList.size) {
            const sortedList = sortHelper.applyStandardSort(favoriteList);
            return state.set('favorites', sortedList);
        }

        return state;
    },
    [ACTION_TYPES.TOGGLE_FAVORITE](state, { addFavorite, branch, payload }) {
        const favoritesList = state.get('favorites');
        let updatedList;

        if (addFavorite) {
            updatedList = favoritesList.push(payload);
        } else {
            const toggledBranchHref = branch._links.self.href;
            // filter the list so that only favorites which didn't match the branch's href are returned
            updatedList = favoritesList.filter(fav => {
                const favoritedBranch = fav.item;
                return !checkMatchingFavoriteUrls(
                    favoritedBranch._links.self.href,
                    toggledBranchHref,
                );
            });
        }

        const sortedList = sortHelper.applyStandardSort(updatedList);
        return state.set('favorites', sortedList);
    },
    [ACTION_TYPES.UPDATE_RUN](state, { jobRun }) {
        const favorites = state.get('favorites');

        for (const fav of favorites) {
            const runsBaseUrl = `${fav.item._links.self.href}runs`;
            const runUrl = jobRun._links.self.href;

            // if the job's run URL starts with the favorited item's '/runs' URL,
            // then the run applies to that item, so update the 'latestRun' property
            if (runUrl.indexOf(runsBaseUrl) === 0) {
                const index = favorites.indexOf(fav);
                const updatedFavorite = clone(fav);
                updatedFavorite.item.latestRun = jobRun;

                const updatedFavorites = favorites.set(index, updatedFavorite);
                const sortedFavorites = sortHelper.applyUpdateSort(updatedFavorites, updatedFavorite);
                return state.set('favorites', sortedFavorites);
            }
        }

        return state;
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
