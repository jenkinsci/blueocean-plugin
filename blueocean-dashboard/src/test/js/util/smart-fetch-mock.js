import * as smartFetch from '../../../main/js/util/smart-fetch';
import debug from 'debug';
var debugLog = debug('smart-fetch:debug'); // same as smart-fetch

module.exports = function mockSmartFetch(path, reply) {
    debugLog("mocking: ", path);
    var exp = new RegExp('.*'+path.replace('/','\\/')+'.*');
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
    smartFetch.paginate = function(urlProvider) {
        var url = urlProvider(0,0);
        debugLog("paginate: ", url, exp);
        if (exp.test(url)) {
            debugLog("got a match! ", url);
            try {
                if (arguments.length > 1) {
                    arguments[length-1](reply);
                }
                return;
            } finally {
                smartFetch.fetch = origFetch;
                smartFetch.paginate = origPaginate;
            }
        }
        return origPaginate.apply(null, arguments);
    };
}
