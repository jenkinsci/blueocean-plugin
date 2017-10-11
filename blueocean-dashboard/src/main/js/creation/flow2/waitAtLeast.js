function time() {
    return new Date().getTime();
}

/**
 * Utility function chained w/ a Promise to delay resolution for a minimum amount of time.
 *
 * Example:
 *    getFooAsync()
 *       .then(waitAtLeast(5000))
 *       .then(value => console.log(value))
 *
 * @param {number} [delay] millis to delay
 * @returns {function} function to pass to 'then()'
 * @deprecated
 */
export default function waitAtLeast(delay = 1000) {
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
        if (time() - begin < delay) {
            promise.payload = data;
            return promise;
        }

        return data;
    };
}
