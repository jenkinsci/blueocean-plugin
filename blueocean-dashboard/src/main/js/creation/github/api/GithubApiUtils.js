/**
 * Append the GitHub "apiUrl" param onto the URL if necessary.
 * @param fullUrl
 * @param apiUrl
 * @returns {String}
 */
function appendApiUrlParam(fullUrl, apiUrl) {
    let appendedUrl = fullUrl;

    if (typeof apiUrl === 'string') {
        if (fullUrl.indexOf('?') === -1) {
            appendedUrl += '?';
        } else {
            appendedUrl += '&';
        }

        appendedUrl += 'apiUrl=';
        // trim trailing slash from URL
        appendedUrl += apiUrl.slice(-1) === '/' ?
            apiUrl.slice(0, -1) : apiUrl;
    }

    return appendedUrl;
}


export default {
    appendApiUrlParam,
};
