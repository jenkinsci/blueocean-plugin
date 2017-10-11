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
        appendedUrl += apiUrl.slice(-1) === '/' ? apiUrl.slice(0, -1) : apiUrl;
    }

    return appendedUrl;
}

/**
 * Extract the protocol and host from a URL.
 * Will not include the trailing slash.
 *
 * @param url
 * @returns {string}
 */
function extractProtocolHost(url) {
    const urlNoQuery = url.split('?')[0];
    const [protocol, hostAndPath] = urlNoQuery.split('//');
    const host = hostAndPath.split('/')[0];
    return `${protocol}//${host}`;
}

export default {
    appendApiUrlParam,
    extractProtocolHost,
};
