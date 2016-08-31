import * as smartFetch from '../../../main/js/util/smart-fetch';
import debug from 'debug';
var debugLog = debug('smart-fetch:debug'); // same as smart-fetch

import urlConfig from '../../../main/js/config.js';

urlConfig.blueoceanAppURL = '/blue';
urlConfig.jenkinsRootURL = '/jenkins'; // maybe just ''?

module.exports = function mockSmartFetch(path, reply) {
    debugLog("mocking: ", path);
    var exp = new RegExp('.*'+path.replace(/([/?=])/g,'\\$1')+'.*');
    var origFetch = smartFetch.fetch;
    var origPaginate = smartFetch.paginate;
    smartFetch.fetch = function(url) {
        debugLog("fetch: ", url, exp);
        if (exp.test(url)) {
            debugLog("got a match! ", url);
            try {
                if (arguments.length > 1) {
                    arguments[length-1](reply);
                }
                if (reply instanceof Error) {
                    return {
                        then() {
                            // ignore
                            return this;
                        },
                        catch(f) {
                            f(reply);
                            return this;
                        },
                    };
                }
                return {
                    then: (fn) => {
                        fn(reply);
                        return {
                            catch: fn => {
                                // do nothing, really
                            }
                        }
                    }
                };
            } finally {
                smartFetch.fetch = origFetch;
                smartFetch.paginate = origPaginate;
            }
        }
        return origFetch.apply(null, arguments);
    };
    smartFetch.paginate = function(opts) {
        var url = opts.urlProvider(0, 0);
        debugLog("paginate: ", url, exp);
        if (exp.test(url)) {
            debugLog("got a match! ", url);
            try {
                if (opts.onData) {
                    opts.onData(reply);
                }
                return {
                    then: (fn) => {
                        fn(reply);
                        return {
                            catch: fn => {
                                // do nothing, really
                            }
                        }
                    }
                };
            } finally {
                smartFetch.fetch = origFetch;
                smartFetch.paginate = origPaginate;
            }
        }
        return origPaginate.apply(null, arguments);
    };
}
