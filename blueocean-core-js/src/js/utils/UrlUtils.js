// TODO: TS

// TODO: file doc

/**
 * Double encode name, feature/test#1 is encoded as feature%252Ftest%25231
 *
 * Branch names are double-encoded in REST urls due to issues on the backend on certain filesystems.
 */
export function doubleUriEncode(input) {
    return encodeURIComponent(encodeURIComponent(input));
}

// general fetchAllTrigger
export const fetchAllSuffix = '?start=0';

/**
 * Returns a new string which ends with a slash, or the
 * original if it already does
 */
export function ensureTrailingSlash(str) {
    if (!str) {
        return str;
    }
    if (str.charAt(str.length - 1) !== '/') {
        return `${str}/`;
    }
    return str;
}
