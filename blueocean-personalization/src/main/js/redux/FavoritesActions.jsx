/**
 * Created by cmeyers on 7/6/16.
 */
import fetch from 'isomorphic-fetch';

import { ACTION_TYPES } from './FavoritesStore';
import urlConfig from '../config';
import moment from 'moment-timezone';
import jwt from 'jsonwebtoken';

urlConfig.loadConfig();

const defaultFetchOptions = {
    credentials: 'same-origin',
};

function authOptions(opts, token) {
      if(!opts.headers) {
          opts.headers = {};
      }

      opts.headers['Authorization']= 'Bearer ' + token
      return opts
}

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

const fetchFlags = {
    [ACTION_TYPES.SET_USER]: false,
    [ACTION_TYPES.SET_FAVORITES]: false,
};

let jwtToken = null;
function storeToken(token) {
    jwtToken = token;
    return token;
}

function getTokenFromStorage() {
    return jwtToken;
}

function getToken() {
    const storedToken = getTokenFromStorage();
    if (storedToken) {
        const tokenPayload = jwt.decode(storedToken);
        const expiry = moment.unix(tokenPayload.exp);
        if (expiry.diff(moment.tz('UTC'), 'seconds') < 300) {
            return Promise.fulfilled(storedToken);
        }
    }

    return fetch('/jenkins/jwt-auth/token', { credentials: 'same-origin' })
        .then(checkStatus)
        .then(response => {
            if (response.headers.get("X-BLUEOCEAN-JWT")) {
                const token = response.headers.get("X-BLUEOCEAN-JWT");
                storeToken(token);
                return token;
            }
            
            throw new Error('Could not fetch jwt_token');
        });
}
export const actions = {
    fetchUser() {
        return (dispatch) => {
            const baseUrl = urlConfig.blueoceanAppURL;
            const url = `${baseUrl}/rest/organizations/jenkins/user/`;
            const fetchOptions = { ...defaultFetchOptions };

            if (fetchFlags[ACTION_TYPES.SET_USER]) {
                return null;
            }

            fetchFlags[ACTION_TYPES.SET_USER] = true;

            return dispatch(actions.generateData(
                { url, fetchOptions },
                ACTION_TYPES.SET_USER
            ));
        };
    },

    fetchFavorites(user) {
        return (dispatch) => {
            const baseUrl = urlConfig.blueoceanAppURL;
            const username = user.id;
            const url = `${baseUrl}/rest/users/${username}/favorites/`;
            const fetchOptions = { ...defaultFetchOptions };

            if (fetchFlags[ACTION_TYPES.SET_FAVORITES]) {
                return null;
            }

            fetchFlags[ACTION_TYPES.SET_FAVORITES] = true;

            return dispatch(actions.generateData(
                { url, fetchOptions },
                ACTION_TYPES.SET_FAVORITES
            ));
        };
    },

    toggleFavorite(addFavorite, branch, favoriteToRemove) {
        return (dispatch) => {
            const baseUrl = urlConfig.jenkinsRootURL;

            const url = addFavorite ?
                `${baseUrl}${branch._links.self.href}/favorite` :
                `${baseUrl}${favoriteToRemove._links.self.href}`;

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
        return (dispatch) => getToken()
            .then(token => fetch(url, authOptions(fetchOptions, token)))
            .then(checkStatus)
            .then(parseJSON)
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
