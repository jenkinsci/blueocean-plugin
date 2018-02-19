function time() {
    return new Date().getTime();
}

/**
 * Utility functions that can be chained w/ a Promise to delay resolution for a *minimum* amount of time.
 * In most cases, use "...delayBoth()" to ensure that resolved and rejected Promises have consistent timing.
 * This is used to perform certain async actions from resolving "too fast" and making the UI jerky.
 *
 * Note that the delay time is not additive to the original call but ensures a minimum across both calls, e.g.
 * 1. If original call takes 250 and delay is 500, Promise will resolve after 500.
 * 2. If original call takes 750 and delay is 500, Promise will resolve after 500.
 *
 * Usage:
 * getFooAsync()
 *     .then(...delayBoth(500)
 *     .then(value => console.log(value), error => console.error(value))
 *
 * getFooAsync()
 *     .then(delayResolve(5000))
 *     .then(value => console.log(value))
 *
 * getFooAsync()
 *     .then(function() {}, delayReject(5000))
 *     .then(function() {}, error => console.error(value))
 */


/**
 * Returns a function that should be chained to a promise to resolve it after the delay.
 *
 * @param {number} [delay] millis to delay
 * @returns {function} resolver function to pass to 'then()'
 */
function delayResolve(delay = 1000) {
    const begin = time();

    const promise = new Promise(resolve => {
        setTimeout(() => {
            if (promise.payload) {
                resolve(promise.payload);
            }
        }, delay);
    });

    return function proceed(data) {
        // if we haven't reached the delay yet, stash the payload
        // so the setTimeout above will resolve it later
        if ((time() - begin) < delay) {
            promise.payload = data;
            return promise;
        }

        return data;
    };
}

/**
 * Returns a function that should be chained to a promise to reject it after the delay.
 *
 * @param {number} [delay] millis to delay
 * @returns {function} rejection function to pass to 'then()'
 */
function delayReject(delay = 1000) {
    const begin = time();

    const promise = new Promise((resolve, reject) => {
        setTimeout(() => {
            if (promise.payload) {
                reject(promise.payload);
            }
        }, delay);
    });

    return function proceed(error) {
        // if we haven't reached the delay yet, stash the payload
        // so the setTimeout above will resolve it later
        if ((time() - begin) < delay) {
            promise.payload = error;
            return promise;
        }

        throw error;
    };
}

/**
 * Returns a function that should be chained to a promise to resolve or reject it after the delay.
 *
 * @param {number} [delay] millis to delay
 * @returns {Array} Array of two functions to pass to 'then()' via spread
 */
function delayBoth(delay = 1000) {
    return [
        delayResolve(delay),
        delayReject(delay),
    ];
}


export default {
    delayResolve,
    delayReject,
    delayBoth,
};
