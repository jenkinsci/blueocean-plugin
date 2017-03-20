import { Fetch, FetchFunctions } from './fetch';

// default impls
const fetchJSON = Fetch.fetchJSON;
const fetch = Fetch.fetch;

export default {

    /**
     * Switches fetch functions with arbitrary replacements.
     * Useful for test spies.
     *
     * @param _fetchJSON
     * @param _fetch
     */
    patchFetch(_fetchJSON, _fetch) {
        Fetch.fetchJSON = _fetchJSON;
        Fetch.fetch = _fetch;
    },

    /**
     * Switches fetch functions for ones that dont use JWT. Needed
     * for running tests.
     */
    patchFetchNoJWT() {
        Fetch.fetchJSON = FetchFunctions.rawFetchJSON;
        Fetch.fetch = FetchFunctions.rawFetch;
    },

    /**
     * Restores original fetch functions.
     */
    restoreFetch() {
        Fetch.fetchJSON = fetchJSON;
        Fetch.fetch = fetch;
    },

    /**
     * Patches fetch functions with a resolved promise. This will make all fetch calls return
     * this data.
     *
     * Usage
     *
     * TestUtils.patchFetchWithData((url, options) => {
     *      assert.equals(url,"someurl")
     *      return { mydata: 5 }
     * })
     */
    patchFetchWithData(dataFn) {
        Fetch.fetchJSON = Fetch.fetch = (url, options) => {
            const { onSuccess, onError } = options || {};

            const data = Promise.resolve(dataFn(url, options));

            if (onSuccess) {
                return data.then(onSuccess).catch(FetchFunctions.onError(onError));
            }

            return data;
        };
    },
};
