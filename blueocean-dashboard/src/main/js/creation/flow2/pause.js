import { Promise } from 'es6-promise';

/**
 * Util to delay resolution of a Promise.
 * Useful for simulating more realistic load times for XHR calls.
 * @param data
 * @param delay
 * @returns {Promise}
 * @deprecated
 */
function pause(data, delay = 1000) {
    return new Promise(resolve => {
        setTimeout(() => resolve(data), delay);
    });
}

export default pause;
