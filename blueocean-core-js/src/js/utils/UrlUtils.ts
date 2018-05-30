/*********************************************************************************************
 **********************************************************************************************

 General URL utility functions.

 For constructing specific URLs (such as ui links to screens, REST or classic links, etc), see
 the UrlBuilder module.

 **********************************************************************************************
 *********************************************************************************************/

/**
 * Double encode name, feature/test#1 is encoded as feature%252Ftest%25231
 *
 * Branch names are double-encoded in REST urls due to issues on the backend on certain filesystems.
 */
export function doubleUriEncode(input: string) {
    return input == null ? '' : encodeURIComponent(encodeURIComponent(input));
}

/**
 * Appended to the end of various URLs to signal you want all of a resource that is normally paged.
 */
export const fetchAllSuffix = '?start=0';

/**
 * Append a slash to the input url if it doesn't end with one already.
 */
export function ensureTrailingSlash(url: string) {
    if (url && url.length > 0 && url.charAt(url.length - 1) !== '/') {
        return `${url}/`;
    }
    return url;
}

export const UrlUtils = {
    doubleUriEncode,
    fetchAllSuffix,
    ensureTrailingSlash,
};
