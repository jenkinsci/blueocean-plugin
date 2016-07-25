/**
 * Created by cmeyers on 7/6/16.
 */
import fetch from 'isomorphic-fetch';

import { ACTION_TYPES } from './FavoritesStore';
import urlConfig from '../config';

urlConfig.loadConfig();

const defaultFetchOptions = {
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
    return response.json()
        // FIXME: workaround for status=200 w/ empty response body that causes error in Chrome
        // server should probably return HTTP 204 instead
        .catch((error) => {
            if (error.message === 'Unexpected end of JSON input') {
                return {};
            }
            throw error;
        });
}

export const actions = {
    fetchUser(config) {
        return (dispatch) => {
            const baseUrl = config.getAppURLBase();
            const url = `${baseUrl}/rest/organizations/jenkins/user/`;
            const fetchOptions = { ...defaultFetchOptions };

            return dispatch(actions.generateData(
                { url, fetchOptions },
                ACTION_TYPES.SET_USER
            ));
        };
    },

    fetchFavorites(config, user) {
        return (dispatch) => {
            const baseUrl = config.getAppURLBase();
            const username = user.id;
            const url = `${baseUrl}/rest/users/${username}/favorites/`;
            const fetchOptions = { ...defaultFetchOptions };

            return dispatch(actions.generateData(
                { url, fetchOptions },
                ACTION_TYPES.SET_FAVORITES
            ));
        };
    },

    toggleFavorite(addFavorite, branch) {
        return (dispatch) => {
            const baseUrl = urlConfig.jenkinsRootURL;
            const url = `${baseUrl}${branch._links.self.href}/favorite`;
            const fetchOptions = {
                ...defaultFetchOptions,
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

    generateData(request, actionType, optional) {
        const { url, fetchOptions } = request;
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
