import jwt from './jwt';
import isoFetch from 'isomorphic-fetch';
import objutils from './objutils.js';

export const FetchFunctions = {
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
        const newOpts = objutils.clone(options);
        newOpts.credentials = newOpts.credentials || 'same-origin';
        return newOpts;
    },
    
    /**
     * Enhances the fetchOptions with the JWT bearer token. Will only be needed
     * if not using fetch or fetchJson.
     */
    jwtFetchOption(token, options = {}) {
        const newOpts = objutils.clone(options);
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
    rawFetchJSON(url, { onSuccess, onError, fetchOptions } = {}) {
        const request = isoFetch(url, FetchFunctions.sameOriginFetchOption(fetchOptions))
            .then(FetchFunctions.checkStatus)
            .then(FetchFunctions.parseJSON);

        if (onSuccess) {
            return request.then(onSuccess).catch(FetchFunctions.onError(onError));
        }
        
        return request;
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
    rawFetch(url, { onSuccess, onError, fetchOptions } = {}) {
        const request = isoFetch(url, FetchFunctions.sameOriginFetchOption(fetchOptions))
            .then(FetchFunctions.checkStatus);

        if (onSuccess) {
            return request.then(onSuccess).catch(FetchFunctions.onError(onError));
        }
      
        return request;
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
    fetchJSON(url, { onSuccess, onError, fetchOptions } = {}) {
        return jwt.getToken()
            .then(token => FetchFunctions.rawFetchJSON(url, {
                onSuccess,
                onError,
                fetchOptions: FetchFunctions.jwtFetchOption(token, fetchOptions),
            }));
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
        return jwt.getToken()
            .then(token => FetchFunctions.rawFetch(url, {
                onSuccess,
                onError,
                fetchOptions: FetchFunctions.jwtFetchOption(token, fetchOptions),
            }));
    },
};

