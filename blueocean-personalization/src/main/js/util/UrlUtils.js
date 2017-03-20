/**
 * Created by cmeyers on 8/15/16.
 */

/**
 * Trims duplicate forward slashes to a single slash and adds trailing slash if needed.
 * @param url
 * @returns {string}
 */
export const cleanSlashes = (url) => {
    if (url.indexOf('//') !== -1) {
        let cleanUrl = url.replace('//', '/');
        cleanUrl = cleanUrl.substr(-1) === '/' ?
            cleanUrl : `${cleanUrl}/`;

        return cleanSlashes(cleanUrl);
    }

    return url;
};

/**
 * Fully decode the provided the string
 * @param {string} value
 * @returns {string}
 */
export const fullUriDecode = (value) => {
    let val = value;
    while (val !== decodeURIComponent(val)) {
        val = decodeURIComponent(val);
    }
    return val;
};

/**
 * Returns a string that has been uri-encoded once.
 * If the string is already encoded one or more times, it will decode it and then re-encode.
 * @param {string} value
 * @returns {string}
 */
export const uriEncodeOnce = (value) => {
    const clean = fullUriDecode(value);
    return encodeURIComponent(clean);
};
