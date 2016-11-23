import es6Promise from 'es6-promise'; es6Promise.polyfill();
import jwt from './jwt';
import isoFetch from 'isomorphic-fetch';
import utils from './utils';
import config from './config';
import dedupe from './utils/dedupe-calls';
import urlconfig from './urlconfig';

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
     * @returns JSON body
     */
    rawFetchJSON(url, { onSuccess, onError, fetchOptions, disableDedupe } = {}) {
        const request = () => {
            const future = isoFetch(url, FetchFunctions.sameOriginFetchOption(fetchOptions))
                .then(FetchFunctions.checkRefreshHeader)
                .then(FetchFunctions.checkStatus)
                .then(FetchFunctions.parseJSON);
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
     * @returns fetch response
     */
    rawFetch(url, { onSuccess, onError, fetchOptions, disableDedupe } = {}) {
        const request = () => {
            const future = isoFetch(url, FetchFunctions.sameOriginFetchOption(fetchOptions))
                .then(FetchFunctions.checkRefreshHeader)
                .then(FetchFunctions.checkStatus);

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
    fetchJSON(url, { onSuccess, onError, fetchOptions, disableCapabilites } = {}) {
        let fixedUrl = url;
        if (urlconfig.getJenkinsRootURL() !== '' && !url.startsWith(urlconfig.getJenkinsRootURL())) {
            fixedUrl = `${urlconfig.getJenkinsRootURL()}${url}`;
        }
        let future;
        if (!config.isJWTEnabled()) {
            future = FetchFunctions.rawFetchJSON(fixedUrl, { onSuccess, onError, fetchOptions });
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
    fetch(url, { onSuccess, onError, fetchOptions } = {}) {
        let fixedUrl = url;
        
        
        if (urlconfig.getJenkinsRootURL() !== '' && !url.startsWith(urlconfig.getJenkinsRootURL())) {
            fixedUrl = `${urlconfig.getJenkinsRootURL()}${url}`;
        }
        if (!config.isJWTEnabled()) {
            return FetchFunctions.rawFetch(fixedUrl, { onSuccess, onError, fetchOptions });
        }
        
        return jwt.getToken()
            .then(token => FetchFunctions.rawFetch(fixedUrl, {
                onSuccess,
                onError,
                fetchOptions: FetchFunctions.jwtFetchOption(token, fetchOptions),
            }));
    },
};

