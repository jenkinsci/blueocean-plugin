/**
 * Created by cmeyers on 7/6/16.
 */
import { UrlConfig, Fetch } from '@jenkins-cd/blueocean-core-js';
import { capabilityAugmenter as augmenter, ToastService } from '@jenkins-cd/blueocean-core-js';

import { ACTION_TYPES } from './FavoritesStore';
import { cleanSlashes } from '../util/UrlUtils';

const fetchFlags = {
    [ACTION_TYPES.SET_FAVORITES]: false,
};

export const actions = {
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

    sortFavorites() {
        return (dispatch) => (
            dispatch({ type: ACTION_TYPES.SORT_FAVORITES })
        );
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
            .then(data => augmenter.augmentCapabilities(data))
            .then((json) => {
                fetchFlags[actionType] = false;
                return dispatch({
                    ...optional,
                    type: actionType,
                    payload: json,
                });
            })
            .catch((error) => {
                const responseBody = error.responseBody;
                if (responseBody && responseBody.code && responseBody.message) {
                    ToastService.newToast({
                        style: 'error',
                        caption: `Favoriting Error (${responseBody.code})`,
                        text: responseBody.message,
                    });
                }

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
