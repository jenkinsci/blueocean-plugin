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
    // fetch helper
    fetchOptions(token, options) {
        let newOptions = {};
        if (options) {
            newOptions = options;
        }

        
        if (!newOptions.credentials) {
            newOptions.credentials = 'same-origin';
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

    /**
     * Fetch JSON data.
     * <p>
     * Utility function that can be mocked for testing.
     *
     * @param url The URL to fetch from.
     * @param onSuccess o
     * @param onError
     */
    fetchJson(url, options) {
        const { onSuccess, onError, fetchOptions } = options || {};
        const request = JWTUtils.getToken()
            .then(token => fetch(url, this.fetchOptions(token, fetchOptions)))
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
};
