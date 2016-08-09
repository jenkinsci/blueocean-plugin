import isoFetch from 'isomorphic-fetch';

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
 * DuplicateCallTracker maintains active fetches against a particular key
 */
export class DuplicateCallTracker {
    constructor() {
        /**
         * Onload callbacks cache. Used to ensure we don't
         * issue multiple in-parallel requests for the same
         * class metadata.
         */
        this.fetchCallbacks = {};
        this.promises = {};
    }

    /**
     * Generalization of consolidation of duplicate requests
     */
    execute(key, promiseProvider, onComplete) {
        let callbacks = this.fetchCallbacks[key];
        if (!callbacks) {
            // This is the first request for this type. Initialize the
            // callback cache and then issue the request
            callbacks = this.fetchCallbacks[key] = onComplete ? [onComplete] : [];

            promiseProvider()
                .then((data) => {
                    // Notify all callbacks
                    for (let i = 0; i < callbacks.length; i++) {
                        try {
                            callbacks[i](data);
                        } catch (e) {
                            console.error('Unexpected Error fulfilling callbacks', e); // eslint-disable-line no-console
                        }
                    }
                    delete this.fetchCallbacks[key];
                    return data; // for downstream .then
                });
        } else {
            // We already have an in-flight request, just store the onComplete callback
            callbacks.push(onComplete);
        }
    }


    /**
     * Generalization of consolidation of duplicate requests
     */
    promise(key, promiseProvider, onComplete) {
        const holder = this.promises[key] || (this.promises[key] = { count: 0, promise: promiseProvider() }); // get or create
        holder.count++;
        holder.promise = holder.promise.then((data) => {
            holder.count--;
            if (holder.count === 0) {
                delete this.promises[key];
            }
            try {
                onComplete(data);
            } catch (e) {
                // TODO how best to handle failures
                console.error(e); // eslint-disable-line no-console
            }
            return data; // for downstream .then
        });
        return holder.promise;
    }
}

const duplicateCallTracker = new DuplicateCallTracker();

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
        return duplicateCallTracker.promise(url, () =>
            isoFetch(url, _onData || fetchOptions) // Fetch data
            .then(checkStatus) // Validate success
            .then(parseJSON) // transfer to json
            .then(successAndFreeze) // add success field & freeze graph
            , (data) => {
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

const fetchPagedData = (pager, concatenator, onData, existingData, first, limit) => {
    // Indicate pending
    onData(assignObj(concatenator(pager, existingData), { $pending: true, $pager: pager }));
    return duplicateCallTracker.promise(pager.urlProvider(first, limit + 1), () =>
        isoFetch(pager.urlProvider(first, limit + 1), fetchOptions) // Fetch data
        .then(checkStatus) // Validate success
        .then(parseJSON) // transfer to json
        .then(successAndFreeze), // add success field & freeze graph
        data => {
            // fetched an extra to test if more
            const hasMore = data.length > limit;
            const outData = assignObj(concatenator(pager, existingData, data));
            outData.$success = true;
            outData.$pager = Object.assign(pager, {
                current: first,
                hasMore,
                currentData: outData,
                startIndex: existingData.length > 0 ? pager.startIndex : first,
            });
            Object.freeze(outData); // already deep frozen
            onData(outData);
        })
        .catch(err => onData(assignObj(concatenator(pager, existingData), { $failed: err })));
};

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
        fetchPagedData(this, this.concatenator, this.onData, this.concatenator(this), first, limit);
    }
    fetchMore() {
        return fetchPagedData(this, this.concatenator, this.onData, this.currentData, this.current + this.pageSize, this.pageSize);
    }
    getTotalPages() {
        return Math.floor((this.startIndex + this.currentData.length) / this.pageSize);
    }
}

/**
 * For data returned as a JSON array (rather than nested in some unknown object structure)
 * this will work to concatenate pages properly, and is the default
 */
export function arrayConcatenator(pager, existing, incoming) {
    if (!existing) {
        return [];
    }
    if (!incoming) {
        return [].concat(existing);
    }
    return existing.concat(incoming.length > pager.pageSize ? incoming.slice(0, -1) : incoming);
}

/**
 * How many records to fetch by default
 */
export const defaultPageSize = 2; // FIXME increase page size

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
export function paginate({ urlProvider, onData, concatenator = arrayConcatenator, startIndex = 0, pageSize = defaultPageSize }) {
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
