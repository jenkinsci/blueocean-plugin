import JWTUtils from './jwtutils';

export default {
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
    sameOriginFetchOption(options) {
        let newOptions = {};
        
        if (options) {
            newOptions = options;
        }
        
        if (!newOptions.credentials) {
            newOptions.credentials = 'same-origin';
        }
        return newOptions;
    },
    
    /**
     * Enhances the fetchOptions with the JWT bearer token. Will only be needed
     * if not using fetch or fetchJson.
     */
    jwtfetchOption(token, options) {
        let newOptions = {};
        if (options) {
            newOptions = options;
        }

        if (!newOptions.headers) {
            newOptions.headers = {};
        }
        
        if (!newOptions.headers.Authorization) {
            newOptions.headers.Authorization = `Bearer ${token}`;
        }

        return newOptions;
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
                this.consoleError(error);
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
    rawFetchJson(url, options) {
        const { onSuccess, onError, fetchOptions } = options || {};
        const request = fetch(url, this.sameOriginFetchOption(fetchOptions))
            .then(this.checkStatus)
            .then(this.parseJSON);

        if (onSuccess) {
            return request.then(onSuccess)
                .catch((error) => {
                    if (onError) {
                        onError(error);
                    } else {
                        console.error(error); // eslint-disable-line no-console
                    }
                });
        }
        

        return request;
    },

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
    fetchJson(url, options) {
        const { onSuccess, onError, fetchOptions } = options || {};
    
        return JWTUtils.getToken()
            .then(token => this.rawFetchJson(url, {
                onSuccess,
                onError,
                fetchOptions: this.jwtfetchOption(token, fetchOptions),
            }));
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
    rawFetch(url, options) {
        const { onSuccess, onError, fetchOptions } = options || {};
        const request = fetch(url, this.sameOriginFetchOption(fetchOptions))
            .then(this.checkStatus);
           
        if (onSuccess) {
            return request.then(onSuccess)
                .catch((error) => {
                    if (onError) {
                        onError(error);
                    } else {
                        console.error(error); // eslint-disable-line no-console
                    }
                });
        }
        

        return request;
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
    fetch(url, options) {
        const { onSuccess, onError, fetchOptions } = options || {};
    
        return JWTUtils.getToken()
            .then(token => this.rawFetch(url, {
                onSuccess,
                onError,
                fetchOptions: this.jwtfetchOption(token, fetchOptions),
            }));
    },
};
