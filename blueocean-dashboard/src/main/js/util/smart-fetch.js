const infoLog = require('debug')('smart-fetch:info');
const debugLog = require('debug')('smart-fetch:debug');
import dedupe from './dedupe-calls';
import { Fetch } from '@jenkins-cd/blueocean-core-js';
import { capabilityAugmenter as augmenter } from '@jenkins-cd/blueocean-core-js';

/**
 * How many records to fetch by default
 */
export const defaultPageSize = 5;

/**
 * Freezes an object and all child properties
 */
const deepFreeze = (obj) => {
    const propNames = Object.getOwnPropertyNames(obj);
    for (let idx = 0; idx < propNames.length; idx++) {
        const prop = obj[propNames[idx]];
        if (prop !== null) {
            if (typeof prop === 'object') {
                deepFreeze(prop);
            }
        }
    }
    return Object.freeze(obj);
};

/**
 * Mark with $success flag and freeze the object
 */
const successAndFreeze = obj => {
    const out = obj;
    out.$success = true;
    deepFreeze(out);
    return out;
};

/**
 * Calls statusCallback with an object holding the current status flag
 * either .$pending, .$success, or .$failed
 * Prevents duplicate fetches for the same URL
 */
export function fetch(url, options, onData) {
    let _onData = onData;
    let _options = options;
    if (!_onData && typeof _options === 'function') {
        _onData = _options;
        _options = null;
    }
    if (_onData) {
        infoLog('fetch: ', url);
        debugLog(' -- pending: ', url);
        _onData({ $pending: true });
        return dedupe(url, () =>
            Fetch.fetchJSON(url, { fetchOptions: _options || {} }) // Fetch data
                .then(data => augmenter.augmentCapabilities(data))
                .then(successAndFreeze)) // add success field & freeze graph
                .then((data) => {
                    debugLog(' -- success: ', url, data);
                    _onData(data);
                })
                .catch(err => {
                    debugLog(' -- error: ', url, err);
                    _onData({ $failed: err });
                });
    }
    // return a fake promise, a thenable
    // so it can be resolved multiple times
    return {
        then: (fn) => fetch(url, _onData, fn),
    };
}

/**
 * Copy paging and fetch data
 */
export function applyFetchMarkers(toObj, fromObj) {
    /* eslint-disable no-param-reassign, no-use-before-define */
    if (fromObj) {
        toObj.$pending = fromObj.$pending;
        toObj.$success = fromObj.$success;
        toObj.$failed = fromObj.$failed;
        if (fromObj.$pager) {
            toObj.$pager = new Pager(
                fromObj.$pager.urlProvider,
                fromObj.$pager.concatenator,
                fromObj.$pager.onData,
                fromObj.$pager.startIndex,
                fromObj.$pager.pageSize,
                toObj); // current data
            toObj.$pager.hasMore = fromObj.$pager.hasMore;
            toObj.$pager.current = fromObj.$pager.current;
        }
    }
    assignObj(toObj);
    /* eslint-enable no-param-reassign, no-use-before-define */
    return toObj;
}

function assignObj(obj, vals) {
    obj.map = function map(...args) { // eslint-disable-line no-param-reassign
        const out = Array.prototype.map.apply(this, args);
        applyFetchMarkers(out, this);
        return out;
    };
    if (vals) {
        Object.assign(obj, vals);
    }
    return obj;
}

class Pager {
    constructor(urlProvider, concatenator, onData, startIndex, pageSize, currentData) {
        this.urlProvider = urlProvider;
        this.concatenator = concatenator;
        this.onData = onData;
        this.startIndex = startIndex;
        this.pageSize = pageSize;
        this.current = startIndex - pageSize; // first will fetchMore the first page
        this.hasMore = true; // assume so
        this.hasPrev = startIndex > 0;
        this.currentData = currentData || concatenator(this);
    }
    fetchRange(first, limit) {
        this._fetchPagedData(this.concatenator, this.onData, this.concatenator(this), first, limit);
    }
    fetchMore() {
        return this._fetchPagedData(this.concatenator, this.onData, this.currentData, this.current + this.pageSize, this.pageSize);
    }
    getTotalPages() {
        return Math.floor((this.startIndex + this.currentData.length) / this.pageSize);
    }
    _fetchPagedData(concatenator, onData, existingData, first, limit) {
        const url = this.urlProvider(first, limit + 1);
        // Indicate pending
        debugLog(' -- pending: ', url);
        onData(assignObj(concatenator(this, existingData), { $pending: true, $pager: this }));
        infoLog('Fetching paged data: ', this);
        return dedupe(url, () =>
            Fetch.fetchJSON(url) // Fetch data
            .then(data => augmenter.augmentCapabilities(data))
            .then(successAndFreeze)) // add success field & freeze graph
            .then(
                (data) => {
                    debugLog(' -- success: ', url, data);
                    // fetched an extra to test if more
                    const hasMore = data.length > limit;
                    const outData = assignObj(concatenator(this, existingData, data));
                    outData.$success = true;
                    outData.$pager = Object.assign(this, {
                        current: first,
                        hasMore,
                        currentData: outData,
                        startIndex: existingData.length > 0 ? this.startIndex : first,
                    });
                    Object.freeze(outData); // children are already frozen, only shallow freeze here
                    onData(outData);
                },
                err => {
                    debugLog(' -- error: ', url, err);
                    onData(assignObj(concatenator(this, existingData), { $failed: err }));
                }
            );
    }
}

/**
 * For data returned as a JSON array (rather than nested in some unknown object structure)
 * this will work to concatenate pages properly, and is the default
 */
function defaultArrayConcatenator(pager, existing, incoming) {
    if (!existing) {
        return [];
    }
    if (!incoming) {
        return existing.slice();
    }
    return existing.concat(incoming.length > pager.pageSize ? incoming.slice(0, -1) : incoming);
}

/**
 * urlProvider as a function that will be passed a startIndex 0-based and a page size
 * and must provide a URL to fetch subsequent data.
 * Easiest to call from named params:
 * paginate({
 *  urlProvider: function,
 *  concatenator: function,
 *  onData: function // optional, may also tread like a promise with a returned .then()
 *  startIndex: int
 *  pageSize: int
 * })
 */
export function paginate({ urlProvider, onData, concatenator = defaultArrayConcatenator, startIndex = 0, pageSize = defaultPageSize }) {
    if (onData) {
        infoLog('paginate: ', urlProvider(0, 0));
        const pager = new Pager(urlProvider, concatenator, onData, startIndex, pageSize);
        return pager.fetchMore();
    }
    // return a fake promise, a thenable
    // so it can be resolved multiple times
    return {
        then: fn => paginate({ urlProvider, concatenator, onData: fn, startIndex, pageSize }),
    };
}
