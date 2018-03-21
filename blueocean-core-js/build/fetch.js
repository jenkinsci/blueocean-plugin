"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
var bluebird_1 = require("bluebird");
require("isomorphic-fetch");
var jwt_1 = require("./jwt");
var utils_1 = require("./utils");
var config_1 = require("./config");
var dedupe_calls_1 = require("./utils/dedupe-calls");
var urlconfig_1 = require("./urlconfig");
var scopes_1 = require("./scopes");
var LoadingIndicator_1 = require("./LoadingIndicator");
var index_1 = require("./capability/index");
var refreshToken = null;
function isGetRequest(fetchOptions) {
    return !fetchOptions || !fetchOptions.method || 'get'.localeCompare(fetchOptions.method) === 0;
}
exports.FetchFunctions = {
    /**
     * Ensures the URL starts with jenkins path if not an absolute URL.
     * @param url
     * @returns {string}
     */
    prefixUrl: function (url) {
        if (url.indexOf('http') === 0) {
            return url;
        }
        if (urlconfig_1.default.getJenkinsRootURL() !== '' && !url.startsWith(urlconfig_1.default.getJenkinsRootURL())) {
            return "" + urlconfig_1.default.getJenkinsRootURL() + url;
        }
        return url;
    },
    checkRefreshHeader: function (response) {
        var _refreshToken = response.headers.get('X-Blueocean-Refresher');
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
            utils_1.default.refreshPage();
            throw new Error('refreshing apge');
        }
        return response;
    },
    /**
     * This method checks for for 2XX http codes. Throws error it it is not.
     * This should only be used if not using fetch or fetchJson.
     */
    checkStatus: function (response) {
        if (response.status >= 300 || response.status < 200) {
            var message = "fetch failed: " + response.status + " for " + response.url;
            var error = new Error(message);
            throw error;
        }
        return response;
    },
    stopLoadingIndicator: function (response) {
        LoadingIndicator_1.default.hide();
        return response;
    },
    /**
     * Adds same-origin option to the fetch.
     */
    sameOriginFetchOption: function (options) {
        if (options === void 0) { options = {}; }
        var newOpts = utils_1.default.clone(options);
        newOpts.credentials = newOpts.credentials || 'same-origin';
        return newOpts;
    },
    /**
     * Enhances the fetchOptions with the JWT bearer token. Will only be needed
     * if not using fetch or fetchJson.
     */
    jwtFetchOption: function (token, options) {
        if (options === void 0) { options = {}; }
        var newOpts = utils_1.default.clone(options);
        newOpts.headers = newOpts.headers || {};
        newOpts.headers["Authorization"] = newOpts.headers["Authorization"] || "Bearer " + token;
        return newOpts;
    },
    /**
     * REturns the json body from the response. It is only needed if
     * you are using FetchUtils.fetch
     *
     * Usage:
     * FetchUtils.fetch(..).then(FetchUtils.parseJSON)
     */
    parseJSON: function (response) {
        return (response
            .json()
            .catch(function (error) {
            if (error.message.indexOf('Unexpected end of JSON input') !== -1) {
                return {};
            }
            throw error;
        }));
    },
    /* eslint-disable no-param-reassign */
    /**
     * Parses the response body for the error generated in checkStatus.
     */
    parseErrorJson: function (error) {
        return error.response.json().then(function (body) {
            error.responseBody = body;
            throw error;
        }, function () {
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
    consoleError: function (error) {
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
    onError: function (errorFunc) {
        return function (error) {
            if (errorFunc) {
                errorFunc(error);
            }
            else {
                exports.FetchFunctions.consoleError(error);
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
    rawFetchJSON: function (url, _a) {
        var _b = _a === void 0 ? {} : _a, onSuccess = _b.onSuccess, onError = _b.onError, fetchOptions = _b.fetchOptions, disableDedupe = _b.disableDedupe, disableLoadingIndicator = _b.disableLoadingIndicator, ignoreRefreshHeader = _b.ignoreRefreshHeader;
        var request = function () {
            var future = getPrefetchedDataFuture(url); // eslint-disable-line no-use-before-define
            if (!disableLoadingIndicator) {
                LoadingIndicator_1.default.show();
            }
            if (!future) {
                future = fetch(url, exports.FetchFunctions.sameOriginFetchOption(fetchOptions));
                if (!ignoreRefreshHeader) {
                    future = future.then(exports.FetchFunctions.checkRefreshHeader);
                }
                future = future.then(exports.FetchFunctions.checkStatus).then(exports.FetchFunctions.parseJSON, exports.FetchFunctions.parseErrorJson);
                if (!disableLoadingIndicator) {
                    future = future.then(exports.FetchFunctions.stopLoadingIndicator, function (err) {
                        exports.FetchFunctions.stopLoadingIndicator(err);
                        throw err;
                    });
                }
            }
            else if (!disableLoadingIndicator) {
                LoadingIndicator_1.default.hide();
            }
            if (onSuccess) {
                return future.then(onSuccess).catch(exports.FetchFunctions.onError(onError));
            }
            return future;
        };
        if (disableDedupe || !isGetRequest(fetchOptions)) {
            return request();
        }
        return dedupe_calls_1.default(url, request);
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
    rawFetch: function (url, _a) {
        var _b = _a === void 0 ? {} : _a, onSuccess = _b.onSuccess, onError = _b.onError, fetchOptions = _b.fetchOptions, disableDedupe = _b.disableDedupe, disableLoadingIndicator = _b.disableLoadingIndicator, ignoreRefreshHeader = _b.ignoreRefreshHeader;
        var request = function () {
            var future = getPrefetchedDataFuture(url); // eslint-disable-line no-use-before-define
            if (!future) {
                if (!disableLoadingIndicator) {
                    LoadingIndicator_1.default.show();
                }
                future = fetch(url, exports.FetchFunctions.sameOriginFetchOption(fetchOptions));
                if (!ignoreRefreshHeader) {
                    future = future.then(exports.FetchFunctions.checkRefreshHeader);
                }
                future = future.then(exports.FetchFunctions.checkStatus);
                if (!disableLoadingIndicator) {
                    future = future.then(exports.FetchFunctions.stopLoadingIndicator, function (err) {
                        exports.FetchFunctions.stopLoadingIndicator(err);
                        throw err;
                    });
                }
            }
            if (onSuccess) {
                return future.then(onSuccess).catch(exports.FetchFunctions.onError(onError));
            }
            return future;
        };
        if (disableDedupe || !isGetRequest(fetchOptions)) {
            return request();
        }
        return dedupe_calls_1.default(url, request);
    },
};
exports.Fetch = {
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
    fetchJSON: function (url, _a) {
        var _b = _a === void 0 ? {} : _a, onSuccess = _b.onSuccess, onError = _b.onError, fetchOptions = _b.fetchOptions, disableCapabilites = _b.disableCapabilites, disableLoadingIndicator = _b.disableLoadingIndicator, ignoreRefreshHeader = _b.ignoreRefreshHeader;
        var fixedUrl = exports.FetchFunctions.prefixUrl(url);
        var future;
        if (!config_1.default.isJWTEnabled()) {
            future = exports.FetchFunctions.rawFetchJSON(fixedUrl, { onSuccess: onSuccess, onError: onError, fetchOptions: fetchOptions, disableLoadingIndicator: disableLoadingIndicator, ignoreRefreshHeader: ignoreRefreshHeader });
        }
        else {
            future = jwt_1.default.getToken().then(function (token) {
                return exports.FetchFunctions.rawFetchJSON(fixedUrl, {
                    onSuccess: onSuccess,
                    onError: onError,
                    fetchOptions: exports.FetchFunctions.jwtFetchOption(token, fetchOptions),
                });
            });
        }
        if (!disableCapabilites) {
            return future.then(function (data) { return index_1.capabilityAugmenter.augmentCapabilities(utils_1.default.clone(data)); });
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
    fetch: function (url, _a) {
        var _b = _a === void 0 ? {} : _a, onSuccess = _b.onSuccess, onError = _b.onError, fetchOptions = _b.fetchOptions, disableLoadingIndicator = _b.disableLoadingIndicator, ignoreRefreshHeader = _b.ignoreRefreshHeader;
        var fixedUrl = exports.FetchFunctions.prefixUrl(url);
        if (!config_1.default.isJWTEnabled()) {
            return exports.FetchFunctions.rawFetch(fixedUrl, { onSuccess: onSuccess, onError: onError, fetchOptions: fetchOptions, disableLoadingIndicator: disableLoadingIndicator, ignoreRefreshHeader: ignoreRefreshHeader });
        }
        return jwt_1.default.getToken().then(function (token) {
            return exports.FetchFunctions.rawFetch(fixedUrl, {
                onSuccess: onSuccess,
                onError: onError,
                fetchOptions: exports.FetchFunctions.jwtFetchOption(token, fetchOptions),
            });
        });
    },
};
function trimRestUrl(url) {
    var REST_PREFIX = 'blue/rest/';
    var prefixOffset = url.indexOf(REST_PREFIX);
    if (prefixOffset !== -1) {
        return url.substring(prefixOffset);
    }
    return url;
}
function getPrefetchedDataFuture(url) {
    var trimmedUrl = trimRestUrl(url);
    for (var prop in scopes_1.prefetchdata) {
        if (scopes_1.prefetchdata.hasOwnProperty(prop)) {
            var preFetchEntry = scopes_1.prefetchdata[prop];
            if (preFetchEntry.restUrl && preFetchEntry.data) {
                // If the trimmed/normalized rest URL matches the url arg supplied
                // to the function, construct a pre-resolved future object containing
                // the prefetched data as the value.
                if (trimRestUrl(preFetchEntry.restUrl) === trimmedUrl) {
                    try {
                        return bluebird_1.default.resolve(JSON.parse(preFetchEntry.data));
                    }
                    finally {
                        // Delete the preFetchEntry i.e. we only use these entries once. So, this
                        // works only for the first request for the data at that URL. Subsequent
                        // calls on that REST endpoint will result in a proper fetch. A local
                        // store needs to be used (redux/mobx etc) if you want to avoid multiple calls
                        // for the same data. This is not a caching layer/mechanism !!!
                        delete scopes_1.prefetchdata[prop];
                    }
                }
            }
        }
    }
    return undefined;
}
