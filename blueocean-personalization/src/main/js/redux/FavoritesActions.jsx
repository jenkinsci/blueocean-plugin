/**
 * Created by cmeyers on 7/6/16.
 */
import fetch from 'isomorphic-fetch';

import { ACTION_TYPES } from './FavoritesStore';
import { cleanSlashes } from '../util/UrlUtils';
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

const fetchFlags = {
    [ACTION_TYPES.SET_USER]: false,
    [ACTION_TYPES.SET_FAVORITES]: false,
};

export const actions = {
    fetchUser() {
        return (dispatch) => {
            const baseUrl = urlConfig.blueoceanAppURL;
            const url = cleanSlashes(`${baseUrl}/rest/organizations/jenkins/user/`);
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
            const url = cleanSlashes(`${baseUrl}/rest/users/${username}/favorites/`);
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

            const url = cleanSlashes(
                addFavorite ?
                `${baseUrl}${branch._links.self.href}/favorite` :
                `${baseUrl}${favoriteToRemove._links.self.href}`
            );

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

    runPipeline(pipeline) {
        return () => {
            const baseUrl = urlConfig.jenkinsRootURL;
            const pipelineUrl = pipeline._links.self.href;
            const runPipelineUrl = cleanSlashes(`${baseUrl}/${pipelineUrl}/runs/`);

            const fetchOptions = {
                ...defaultFetchOptions,
                method: 'PUT',
                headers: {
                    'Content-Type': 'application/json',
                },
            };

            // once job is queued, SSE will fire and trigger "updateRun" so no need to dispatch an action here
            fetch(runPipelineUrl, fetchOptions);
        };
    },

    replayPipeline(pipeline) {
        return () => {
            const baseUrl = urlConfig.jenkinsRootURL;
            const latestRunUrl = pipeline.latestRun._links.self.href;
            const replayPipelineUrl = cleanSlashes(`${baseUrl}/${latestRunUrl}/replay/`);

            const fetchOptions = {
                ...defaultFetchOptions,
                method: 'PUT',
                headers: {
                    'Content-Type': 'application/json',
                },
            };

            // once job is queued, SSE will fire and trigger "updateRun" so no need to dispatch an action here
            fetch(replayPipelineUrl, fetchOptions);
        };
    },

    stopPipeline(pipeline) {
        return () => {
            const baseUrl = urlConfig.blueoceanAppURL;
            const latestRunUrl = pipeline.latestRun._links.self.href;
            const stopPipelineUrl = cleanSlashes(`${baseUrl}/${latestRunUrl}/stop/?blocking=true&timeOutInSecs=10`);

            const fetchOptions = {
                ...defaultFetchOptions,
                method: 'PUT',
                headers: {
                    'Content-Type': 'application/json',
                },
            };

            // once job is queued, SSE will fire and trigger "updateRun" so no need to dispatch an action here
            fetch(stopPipelineUrl, fetchOptions);
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
        return (dispatch) => fetch(url, fetchOptions)
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
