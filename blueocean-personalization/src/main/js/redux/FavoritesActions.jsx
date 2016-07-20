/**
 * Created by cmeyers on 7/6/16.
 */
import fetch from 'isomorphic-fetch';

import { ACTION_TYPES } from './FavoritesStore';

const fetchOptions = {
    credentials: 'same-origin',
};

function checkStatus(response) {
    if (response.status >= 300 || response.status < 200) {
        const error = new Error(response.statusText);
        error.response = response;
        throw error;
    }
    return response;
}

function parseJSON(response) {
    return response.json();
}

export const actions = {
    fetchUser(config) {
        return (dispatch) => {
            const baseUrl = config.getAppURLBase();
            const url = `${baseUrl}/rest/organizations/jenkins/user/`;

            return dispatch(actions.generateData(
                url,
                ACTION_TYPES.SET_USER
            ));
        };
    },

    fetchFavorites(config, user) {
        return (dispatch) => {
            const baseUrl = config.getAppURLBase();
            const username = user.id;
            const url = `${baseUrl}/rest/users/${username}/favorites/`;

            return dispatch(actions.generateData(
                url,
                ACTION_TYPES.SET_FAVORITES
            ));
        };
    },

    generateData(url, actionType, optional) {
        return (dispatch) => fetch(url, fetchOptions)
            .then(checkStatus)
            .then(parseJSON)
            .then(json => dispatch({
                ...optional,
                type: actionType,
                payload: json,
            }))
            .catch((error) => {
                console.error(error); // eslint-disable-line no-console
                // call again with no payload so actions handle missing data
                dispatch({
                    ...optional,
                    type: actionType,
                });
            });
    },
};
