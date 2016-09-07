/**
 * Created by cmeyers on 7/6/16.
 */

import { ACTION_TYPES } from './FavoritesStore';
import { UrlConfig, Fetch } from '@jenkins-cd/blueocean-core-js';
import { cleanSlashes } from '../util/UrlUtils';

const fetchFlags = {
    [ACTION_TYPES.SET_USER]: false,
    [ACTION_TYPES.SET_FAVORITES]: false,
};

export const actions = {
    fetchUser() {
        return (dispatch) => {
            const baseUrl = UrlConfig.getBlueOceanAppURL();
            const url = cleanSlashes(`${baseUrl}/rest/organizations/jenkins/user/`);

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
            const baseUrl = UrlConfig.getBlueOceanAppURL();
            const username = user.id;
            const url = cleanSlashes(`${baseUrl}/rest/users/${username}/favorites/`);

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
            const baseUrl = UrlConfig.getJenkinsRootURL();
            const url = cleanSlashes(addFavorite ?
                `${baseUrl}${branch._links.self.href}/favorite` :
                `${baseUrl}${favoriteToRemove._links.self.href}`
            );


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

    runPipeline(pipeline) {
        return () => {
            const baseUrl = UrlConfig.getJenkinsRootURL();
            const pipelineUrl = pipeline._links.self.href;
            const runPipelineUrl = cleanSlashes(`${baseUrl}/${pipelineUrl}/runs/`);

            const fetchOptions = {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
            };

            // once job is queued, SSE will fire and trigger "updateRun" so no need to dispatch an action here
            Fetch.fetch(runPipelineUrl, { fetchOptions });
        };
    },

    replayPipeline(pipeline) {
        return () => {
            const baseUrl = UrlConfig.getJenkinsRootURL();
            const pipelineUrl = pipeline.latestRun._links.self.href;
            const runPipelineUrl = cleanSlashes(`${baseUrl}/${pipelineUrl}/replay/`);

            const fetchOptions = {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
            };

            // once job is queued, SSE will fire and trigger "updateRun" so no need to dispatch an action here
            Fetch.fetch(runPipelineUrl, { fetchOptions });
        };
    },

    stopPipeline(pipeline) {
        return () => {
            const baseUrl = UrlConfig.getJenkinsRootURL();
            const latestRunUrl = pipeline.latestRun._links.self.href;
            const stopPipelineUrl = cleanSlashes(`${baseUrl}/${latestRunUrl}/stop/?blocking=true&timeOutInSecs=10`);

            const fetchOptions = {
                method: 'PUT',
                headers: {
                    'Content-Type': 'application/json',
                },
            };

            // once job is stopped, SSE will fire and trigger "updateRun" so no need to dispatch an action here
            Fetch.fetch(stopPipelineUrl, fetchOptions);
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
        return (dispatch) => Fetch.fetchJSON(url, { fetchOptions })
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
