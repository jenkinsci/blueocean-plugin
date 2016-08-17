/**
 * Created by cmeyers on 7/6/16.
 */

import { ACTION_TYPES } from './FavoritesStore';
import { UrlUtils, FetchUtils } from '@jenkins-cd/blueocean-core-js';

const fetchFlags = {
    [ACTION_TYPES.SET_USER]: false,
    [ACTION_TYPES.SET_FAVORITES]: false,
};

export const actions = {
    fetchUser() {
        return (dispatch) => {
            const baseUrl = UrlUtils.getBlueAppUrl();
            const url = `${baseUrl}/rest/organizations/jenkins/user/`;
          
            if (fetchFlags[ACTION_TYPES.SET_USER]) {
                return null;
            }

            fetchFlags[ACTION_TYPES.SET_USER] = true;

            return dispatch(actions.generateData(
                { url },
                ACTION_TYPES.SET_USER
            ));
        };
    },

    fetchFavorites(user) {
        return (dispatch) => {
            const baseUrl = UrlUtils.getBlueAppUrl();
            const username = user.id;
            const url = `${baseUrl}/rest/users/${username}/favorites/`;
           
            if (fetchFlags[ACTION_TYPES.SET_FAVORITES]) {
                return null;
            }

            fetchFlags[ACTION_TYPES.SET_FAVORITES] = true;

            return dispatch(actions.generateData(
                { url },
                ACTION_TYPES.SET_FAVORITES
            ));
        };
    },

    toggleFavorite(addFavorite, branch, favoriteToRemove) {
        return (dispatch) => {
            const baseUrl = UrlUtils.getBlueAppUrl();
           
            const url = addFavorite ?
                `${baseUrl}${branch._links.self.href}/favorite` :
                `${baseUrl}${favoriteToRemove._links.self.href}`;

            const fetchOptions = {
                method: 'PUT',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify(
                    { favorite: addFavorite }
                ),
            };

            return dispatch(actions.generateData(
                { url, fetchOptions },
                ACTION_TYPES.TOGGLE_FAVORITE,
                { addFavorite, branch },
            ));
        };
    },

    updateRun(jobRun) {
        return (dispatch) => {
            dispatch({
                type: ACTION_TYPES.UPDATE_RUN,
                jobRun,
            });
        };
    },

    generateData(request, actionType, optional) {
        const { url, fetchOptions } = request;
        return (dispatch) => FetchUtils.fetchJson(url, { fetchOptions })
            .then((json) => {
                fetchFlags[actionType] = false;
                return dispatch({
                    ...optional,
                    type: actionType,
                    payload: json,
                });
            })
            .catch((error) => {
                fetchFlags[actionType] = false;
                console.error(error); // eslint-disable-line no-console
                // call again with no payload so actions handle missing data
                dispatch({
                    ...optional,
                    type: actionType,
                    payload: error,
                });
            });
    },
};
