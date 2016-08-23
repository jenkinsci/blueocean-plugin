import JWTUtils from './jwtutils';

export default {
    checkStatus(response) {
        if (response.status >= 300 || response.status < 200) {
            const error = new Error(response.statusText);
            error.response = response;
            throw error;
        }
        return response;
    },
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
    // fetch helper
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

    consoleError(error) {
        console.error(error); // eslint-disable-line no-console
    },

    onError(errorFunc) {
        return error => {
            if (errorFunc) {
                errorFunc(error);
            } else {
                this.consoleError(error);
            }
        };
    },
    
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
     * @param {string} [options.onSuccess] -
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
