/**
 * Created by cmeyers on 7/6/16.
 */
import fetch from 'isomorphic-fetch';

import { ACTION_TYPES } from './FavoritesStore';

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
        // FIXME: workaround for empty response body for causes error in Chrome
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

    toggleFavorite(config, pipeline, favorite) {
        return (dispatch) => {
            const baseUrl = config.getRootURL();
            const url = `${baseUrl}/${pipeline._links.self.href}/favorite`;
            const fetchOptions = {
                ...defaultFetchOptions,
                method: 'PUT',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify(
                    { favorite }
                ),
            };

            // TODO: need to validate that payload is passing through after JENKINS-36580 is complete
            return dispatch(actions.generateData(
                { url, fetchOptions },
                ACTION_TYPES.TOGGLE_FAVORITE,
                { pipeline, favorite },
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
