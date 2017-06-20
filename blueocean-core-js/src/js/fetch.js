import es6Promise from 'es6-promise'; es6Promise.polyfill();
import jwt from './jwt';
import isoFetch from 'isomorphic-fetch';
import utils from './utils';
import config from './config';
import dedupe from './utils/dedupe-calls';
import urlconfig from './urlconfig';
import { prefetchdata } from './scopes';
import loadingIndicator from './LoadingIndicator';

const Promise = es6Promise.Promise;

import { capabilityAugmenter } from './capability/index';
let refreshToken = null;
export const FetchFunctions = {
    checkRefreshHeader(response) {
        const _refreshToken = response.headers.get('X-Blueocean-Refresher');
        // No token in response, lets just ignore.
        if (!_refreshToken) {
            return response;
        }

        // First time we have seen a refresh token, early exit.
        if (!refreshToken) {
            refreshToken = _refreshToken;
            return response;
        }

        // We need to refresh the page now!
        if (refreshToken !== _refreshToken) {
            utils.refreshPage();
            throw new Error('refreshing apge');
        }
        return response;
    },

    /**
     * This method checks for for 2XX http codes. Throws error it it is not.
     * This should only be used if not using fetch or fetchJson.
     */
    checkStatus(response) {
        if (response.status >= 300 || response.status < 200) {
            const error = new Error(response.statusText);
            error.response = response;
            throw error;
        }
        return response;
    },

    stopLoadingIndicator(response) {
        loadingIndicator.hide();
        return response;
    },

    /**
     * Adds same-origin option to the fetch.
     */
    sameOriginFetchOption(options = {}) {
        const newOpts = utils.clone(options);
        newOpts.credentials = newOpts.credentials || 'same-origin';
        return newOpts;
    },

    /**
     * Enhances the fetchOptions with the JWT bearer token. Will only be needed
     * if not using fetch or fetchJson.
     */
    jwtFetchOption(token, options = {}) {
        const newOpts = utils.clone(options);
        newOpts.headers = newOpts.headers || {};
        newOpts.headers.Authorization = newOpts.headers.Authorization || `Bearer ${token}`;
        return newOpts;
    },

    /**
     * REturns the json body from the response. It is only needed if
     * you are using FetchUtils.fetch
     *
     * Usage:
     * FetchUtils.fetch(..).then(FetchUtils.parseJSON)
     */
    parseJSON(response) {
        return response.json()
        // FIXME: workaround for status=200 w/ empty response body that causes error in Chrome
        // server should probably return HTTP 204 instead
        .catch((error) => {
            if (error.message === 'Unexpected end of JSON input') {
                return {};
            }
            throw error;
        });
    },

    /* eslint-disable no-param-reassign */
    /**
     * Parses the response body for the error generated in checkStatus.
     */
    parseErrorJson(error) {
        return error.response.json().then(
            body => {
                error.responseBody = body;
                throw error;
            },
            () => {
                error.responseBody = null;
                throw error;
            });
    },
    /* eslint-enable no-param-reassign */

     /**
     * Error function helper to log errors to console.
     *
     * Usage;
     * fetchJson(..).catch(FetchUtils.consoleError)
     */
    consoleError(error) {
        console.error(error); // eslint-disable-line no-console
    },

    /**
     * Error function helper to call a callback on a rejected promise.
     * if callback is null, log to console). Use .catch() if you know it
     * will not be null though.
     *
     * Usage;
     * fetchJson(..).catch(FetchUtils.onError(error => //do something)
     */
    onError(errorFunc) {
        return error => {
            if (errorFunc) {
                errorFunc(error);
            } else {
                FetchFunctions.consoleError(error);
            }
        };
    },

     /**
     * Raw fetch that returns the json body.
     *
     * This method is semi-private, under normal conditions it should not be
     * used as it does not include the JWT bearer token
     *
     * @param {string} url - The URL to fetch from.
     * @param {Object} [options]
     * @param {function} [options.onSuccess] - Optional callback success function.
     * @param {function} [options.onError] - Optional error callback.
     * @param {Object} [options.fetchOptions] - Optional isomorphic-fetch options.
     * @param {boolean} [options.disableDedupe] - Optional flag to disable dedupe for this request.
     * @param {boolean} [options.disableLoadingIndicator] - Optional flag to disable loading indicator for this request.
     * @returns JSON body
     */
    rawFetchJSON(url, { onSuccess, onError, fetchOptions, disableDedupe, disableLoadingIndicator, ignoreRefreshHeader } = {}) {
        const request = () => {
            let future = getPrefetchedDataFuture(url); // eslint-disable-line no-use-before-define

            if (!disableLoadingIndicator) {
                loadingIndicator.show();
            }

            if (!future) {
                future = isoFetch(url, FetchFunctions.sameOriginFetchOption(fetchOptions));

                if (!ignoreRefreshHeader) {
                    future = future.then(FetchFunctions.checkRefreshHeader);
                }

                future = future.then(FetchFunctions.checkStatus)
                    .then(FetchFunctions.parseJSON, FetchFunctions.parseErrorJson);

                if (!disableLoadingIndicator) {
                    future = future.then(FetchFunctions.stopLoadingIndicator, err => { FetchFunctions.stopLoadingIndicator(); throw err; });
                }
            } else if (!disableLoadingIndicator) {
                loadingIndicator.hide();
            }
            if (onSuccess) {
                return future.then(onSuccess).catch(FetchFunctions.onError(onError));
            }

            return future;
        };
        if (disableDedupe) {
            return request();
        }

        return dedupe(url, request);
    },
    /**
     * Raw fetch.
     *
     * This method is semi-private, under normal conditions it should not be
     * used as it does not include the JWT bearer token
     *
     * @param {string} url - The URL to fetch from.
     * @param {Object} [options]
     * @param {function} [options.onSuccess] - Optional callback success function.
     * @param {function} [options.onError] - Optional error callback.
     * @param {Object} [options.fetchOptions] - Optional isomorphic-fetch options.
     * @param {boolean} [options.disableDedupe] - Optional flag to disable dedupe for this request.
     * @param {boolean} [options.disableLoadingIndicator] - Optional flag to disable loading indicator for this request.
     * @returns fetch response
     */
    rawFetch(url, { onSuccess, onError, fetchOptions, disableDedupe, disableLoadingIndicator, ignoreRefreshHeader } = {}) {
        const request = () => {
            let future = getPrefetchedDataFuture(url); // eslint-disable-line no-use-before-define
            if (!future) {
                if (!disableLoadingIndicator) {
                    loadingIndicator.show();
                }

                future = isoFetch(url, FetchFunctions.sameOriginFetchOption(fetchOptions));

                if (!ignoreRefreshHeader) {
                    future = future.then(FetchFunctions.checkRefreshHeader);
                }

                future = future.then(FetchFunctions.checkStatus);

                if (!disableLoadingIndicator) {
                    future = future.then(FetchFunctions.stopLoadingIndicator, err => { FetchFunctions.stopLoadingIndicator(); throw err; });
                }
            }
            if (onSuccess) {
                return future.then(onSuccess).catch(FetchFunctions.onError(onError));
            }
            return future;
        };

        if (disableDedupe) {
            return request();
        }

        return dedupe(url, request);
    },
};

export const Fetch = {
    /**
     * Fetch JSON data.
     * <p>
     * Utility function that can be mocked for testing.
     *
     * @param {string} url - The URL to fetch from.
     * @param {Object} [options]
     * @param {function} [options.onSuccess] - Optional callback success function.
     * @param {function} [options.onError] - Optional error callback.
     * @param {Object} [options.fetchOptions] - Optional isomorphic-fetch options.
     * @returns JSON body.
     */
    fetchJSON(url, { onSuccess, onError, fetchOptions, disableCapabilites, ignoreRefreshHeader } = {}) {
        let fixedUrl = url;
        if (urlconfig.getJenkinsRootURL() !== '' && !url.startsWith(urlconfig.getJenkinsRootURL())) {
            fixedUrl = `${urlconfig.getJenkinsRootURL()}${url}`;
        }
        let future;
        if (!config.isJWTEnabled()) {
            future = FetchFunctions.rawFetchJSON(fixedUrl, { onSuccess, onError, fetchOptions, ignoreRefreshHeader });
        } else {
            future = jwt.getToken()
                .then(token => FetchFunctions.rawFetchJSON(fixedUrl, {
                    onSuccess,
                    onError,
                    fetchOptions: FetchFunctions.jwtFetchOption(token, fetchOptions),
                }));
        }

        if (!disableCapabilites) {
            return future.then(data => capabilityAugmenter.augmentCapabilities(utils.clone(data)));
        }

        return future;
    },

    /**
     * Fetch data.
     * <p>
     * Utility function that can be mocked for testing.
     *
     * @param {string} url - The URL to fetch from.
     * @param {Object} [options]
     * @param {function} [options.onSuccess] - Optional callback success function.
     * @param {function} [options.onError] - Optional error callback.
     * @param {Object} [options.fetchOptions] - Optional isomorphic-fetch options.
     * @returns fetch body.
     */
    fetch(url, { onSuccess, onError, fetchOptions, disableLoadingIndicator, ignoreRefreshHeader } = {}) {
        let fixedUrl = url;

        if (urlconfig.getJenkinsRootURL() !== '' && !url.startsWith(urlconfig.getJenkinsRootURL())) {
            fixedUrl = `${urlconfig.getJenkinsRootURL()}${url}`;
        }
        if (!config.isJWTEnabled()) {
            return FetchFunctions.rawFetch(fixedUrl, { onSuccess, onError, fetchOptions, disableLoadingIndicator, ignoreRefreshHeader });
        }

        return jwt.getToken()
            .then(token => FetchFunctions.rawFetch(fixedUrl, {
                onSuccess,
                onError,
                fetchOptions: FetchFunctions.jwtFetchOption(token, fetchOptions),
            }));
    },
};

function trimRestUrl(url) {
    const REST_PREFIX = 'blue/rest/';
    const prefixOffset = url.indexOf(REST_PREFIX);

    if (prefixOffset !== -1) {
        return url.substring(prefixOffset);
    }

    return url;
}

function getPrefetchedDataFuture(url) {
    const trimmedUrl = trimRestUrl(url);

    for (const prop in prefetchdata) {
        if (prefetchdata.hasOwnProperty(prop)) {
            const preFetchEntry = prefetchdata[prop];
            if (preFetchEntry.restUrl && preFetchEntry.data) {
                // If the trimmed/normalized rest URL matches the url arg supplied
                // to the function, construct a pre-resolved future object containing
                // the prefetched data as the value.
                if (trimRestUrl(preFetchEntry.restUrl) === trimmedUrl) {
                    try {
                        return Promise.resolve(JSON.parse(preFetchEntry.data));
                    } finally {
                        // Delete the preFetchEntry i.e. we only use these entries once. So, this
                        // works only for the first request for the data at that URL. Subsequent
                        // calls on that REST endpoint will result in a proper fetch. A local
                        // store needs to be used (redux/mobx etc) if you want to avoid multiple calls
                        // for the same data. This is not a caching layer/mechanism !!!
                        delete prefetchdata[prop];
                    }
                }
            }
        }
    }

    return undefined;
}
