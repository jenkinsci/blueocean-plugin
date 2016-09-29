/**
 * Created by cmeyers on 7/6/16.
 */
import es6Promise from 'es6-promise'; es6Promise.polyfill();

import { Fetch, QueueUtils, Rest, UrlConfig } from '@jenkins-cd/blueocean-core-js';
import { capabilityAugmenter as augmenter } from '@jenkins-cd/blueocean-core-js';

import { ACTION_TYPES } from './FavoritesStore';
import { cleanSlashes } from '../util/UrlUtils';
import PersonalizationRest from '../rest/index';

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
            if (fetchFlags[ACTION_TYPES.SET_FAVORITES]) {
                return null;
            }

            fetchFlags[ACTION_TYPES.SET_FAVORITES] = true;

            // TODO: refactor this pyramid of doom into something more comprehensible
            const promise = PersonalizationRest.favoritesApi.fetchFavorites(user)
                .then(data => augmenter.augmentCapabilities(data))
                .then(favorites => {
                    // find any favorites that have at least one queued item
                    const queuedItems = favorites
                        .map(favorite => favorite.item)
                        .filter(item => item.numberOfQueuedPipelines > 0);

                    if (queuedItems.length) {
                        // fetch the queue for each item that has a non-empty queue
                        const requests = queuedItems.map(item => {
                            return Rest.queueApi.fetchQueue(item);
                        });

                        return Promise.all(requests).then(queues => {
                            // update each favorite's "item.latestRun" property with a pseudo-run from its queue item
                            queues.forEach((queue, index) => {
                                const item = queuedItems[index];

                                if (queue.length) {
                                    item.latestRun = QueueUtils.mapQueueImplToPseudoRun(queue[0]);
                                }
                            });

                            return favorites;
                        });
                    } else {
                        return favorites;
                    }
                });

            return promise.then((favorites) => {
                fetchFlags[ACTION_TYPES.SET_FAVORITES] = false;

                return dispatch({
                    type: ACTION_TYPES.SET_FAVORITES,
                    payload: favorites,
                });
            });
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
