import isoFetch from 'isomorphic-fetch';
import dedupe from './dedupe-calls';

/**
 * How many records to fetch by default
 */
export const defaultPageSize = 2; // FIXME increase page size

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
 * Validates the status is 200-299 or returns an error
 */
const checkStatus = (response) => {
    if (response.status >= 300 || response.status < 200) {
        console.error('ERROR: ', response); // eslint-disable-line no-console
        const error = new Error(response.statusText);
        error.response = response;
        throw error;
    }
    return response;
};

/**
 * Get the JSON response body
 */
const parseJSON = (rsp) => {
    try {
        return rsp.json();
    } catch (err) {
        console.error('Unable to parse JSON: ', rsp.body, err); // eslint-disable-line no-console
        throw new Error('Invalid JSON payload', err);
    }
};

/**
 * Static fetch options used for every request
 */
const fetchOptions = { credentials: 'same-origin' };

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
        _onData({ $pending: true });
        return dedupe(url, () =>
            isoFetch(url, _options || fetchOptions) // Fetch data
            .then(checkStatus) // Validate success
            .then(parseJSON) // transfer to json
            .then(successAndFreeze) // add success field & freeze graph
            )
            .then((data) => {
                _onData(data);
            })
            .catch(err => {
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
    if (fromObj) {
        for (const k in fromObj) {
            if (k && k.indexOf && k.indexOf('$') === 0) {
                toObj[k] = fromObj[k]; // eslint-disable-line no-param-reassign
            }
        }
    }
    assignObj(toObj); // eslint-disable-line no-use-before-define
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
    constructor(urlProvider, concatenator, onData, startIndex, pageSize) {
        this.urlProvider = urlProvider;
        this.concatenator = concatenator;
        this.onData = onData;
        this.startIndex = startIndex;
        this.pageSize = pageSize;
        this.current = startIndex - pageSize; // first will fetchMore the first page
        this.hasMore = true; // assume so
        this.hasPrev = startIndex > 0;
        this.currentData = concatenator(this);
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
        // Indicate pending
        onData(assignObj(concatenator(this, existingData), { $pending: true, $pager: this }));
        const url = this.urlProvider(first, limit + 1);
        return dedupe(url, () =>
            isoFetch(url, fetchOptions) // Fetch data
            .then(checkStatus) // Validate success
            .then(parseJSON) // transfer to json
            .then(successAndFreeze) // add success field & freeze graph
            )
            .then((data) => {
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
            })
            .catch(err => onData(assignObj(concatenator(this, existingData), { $failed: err })));
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
        return [].concat(existing);
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
        const pager = new Pager(urlProvider, concatenator, onData, startIndex, pageSize);
        return pager.fetchMore();
    }
    // return a fake promise, a thenable
    // so it can be resolved multiple times
    return {
        then: fn => paginate({ urlProvider, concatenator, onData: fn, startIndex, pageSize }),
    };
}
