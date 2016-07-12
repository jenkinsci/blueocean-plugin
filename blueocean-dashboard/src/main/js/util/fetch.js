/**
 * This is a small-ish wrapper around isomorphic-fetch,
 * which exposes a redux store that tracks all active
 * fetches and dispatches status changes.
 */

import fetch from 'isomorphic-fetch';

/**
 * Holds fetch status during fetch operations
 */
export class FetchStatus {
    constructor(item) {
        this.item = item;
    }
    
    static isValue(result) {
        return !(result instanceof FetchStatus);
    }
}

export class Success extends FetchStatus {
    constructor(item, payload) {
        super(item);
        this.payload = payload;
    }
}

export class Pending extends FetchStatus {
    constructor(item, requestData) {
        super(item);
        this.requestData = requestData;
    }
}

export class Failure extends FetchStatus {
    constructor(item, message, cause) {
        super(item);
        this.message = message;
        this.cause = cause;
    }
}

export class FetchStatusStore {
    constructor() {
        this._active = [];
    }
    onStart(id) {
        console.log('onStart');
        this._active.push(id);
    }
    onComplete(id) {
        const idx = this._active.indexOf(id);
        if (idx >= 0) {
            this._active.splice(idx, 1);
            console.log('onComplete');
        } else {
            throw new Failure(`Invalid fetch id: ${id}`);
        }
    }
}

export const store = new FetchStatusStore();

export const isValue = FetchStatus.isValue;
export const isSuccess = isValue; // for now, non-status message is success
export const isPending = v => v instanceof Pending;
export const isFailure = v => v instanceof Failure;

function checkStatus(response) {
    if (response.status >= 300 || response.status < 200) {
        const error = new Error(response.statusText);
        error.response = response;
        throw error;
    }
    return response;
}

const fetcher = (...args) => {
    const fetchId = {};
    store.onStart(fetchId);
    
    const fetchPromise = fetch.apply(this, args);
    return fetchPromise
        .then(checkStatus)
        .then((response) => {
            store.onComplete(fetchId);
            return response;
        })
        .catch((err) => {
            store.onComplete(fetchId);
            console.error(err); // eslint-disable-line no-console
            throw err;
        });
};

export default fetcher;
