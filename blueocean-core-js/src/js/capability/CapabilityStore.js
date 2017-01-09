/**
 * Created by cmeyers on 8/31/16.
 */
import es6Promise from 'es6-promise'; es6Promise.polyfill();
import { installInfo } from '../storage';

// Create a dedicated storage namespace that we use to store classes
// info in the browser, eliminating client REST call overhead for classes
// info. This storage namespace will be auto-cleared if the jesnkins version
// changes, or if the active plugins change.
const classesInfoNS = installInfo.subspace('classesInfo');

/**
 * Retrieves capability metadata for class names.
 * Uses an internal cache to minimize REST API calls.
 */
export class CapabilityStore {

    constructor(capabilityApi) {
        this._localStore = {};
        this._capabilityApi = capabilityApi;
    }

    /**
     * Fetch the capabilities for one or more class names.
     * Will used cached values if available.
     *
     * @param classNames
     * @returns {Promise} with fulfilled {object} keyed by className, with an array of string capability names.
     */
    resolveCapabilities(...classNames) {
        const result = {};
        const classesToFetch = [];

        // determine which class names are already in the cache and which aren't
        for (const className of classNames) {
            const classInfo = this._getStoredClassInfo(className);
            if (classInfo) {
                result[className] = classInfo;
            } else {
                classesToFetch.push(className);
            }
        }

        // if nothing to fetch, just return an immediately fulfilled Promise
        if (classesToFetch.length === 0) {
            return new Promise(resolve => resolve(result));
        }

        // fetch the capabilities and then merge that with the values already in the cache
        return this._fetchCapabilities(classesToFetch)
            .then(fetchedCapabilities => Object.assign(result, fetchedCapabilities));
    }

    /**
     * Fetch the capabilities for one or more class names.
     *
     * @param classNames
     * @returns {Promise} with fulfilled {object} keyed by className, with an array of string capability names.
     * @private
     */
    _fetchCapabilities(classNames) {
        return this._capabilityApi.fetchCapabilities(classNames)
            .then(data => this._storeCapabilities(data.map));
    }

    /**
     * Store the values in the cache and return it.
     *
     * @param map
     * @returns {object} keyed by className, with an array of string capability names.
     * @private
     */
    _storeCapabilities(map) {
        const storedCapabilities = {};

        Object.keys(map).forEach(className => {
            const capabilities = map[className];
            this._localStore[className] = storedCapabilities[className] = capabilities.classes.slice();
            // Also store in the browser so we don't have to look
            // up this info again (unless the storage namespace is
            // cleared due to jenkins or plugin changes).
            classesInfoNS.set(className, this._localStore[className]);
        });

        return storedCapabilities;
    }

    _getStoredClassInfo(className) {
        if (!this._localStore[className]) {
            // If we don't have a copy of the class info in the localStore,
            // check the browser storage and copy it into the localStore.
            // We still want to use the localStore because it holds deserialized
            // copies of the class info, which means that a localStore lookup
            // would be lower overhead and probably faster.
            this._localStore[className] = classesInfoNS.get(className);
        }
        return this._localStore[className];
    }
}
