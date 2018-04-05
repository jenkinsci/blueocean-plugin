import AppConfig from '../config';

// TODO: TS

// TODO: file doc

/**
 * Double encode name, feature/test#1 is encoded as feature%252Ftest%25231
 */
export const doubleUriEncode = input => encodeURIComponent(encodeURIComponent(input));

// general fetchAllTrigger
export const fetchAllSuffix = '?start=0';

// Add fetchAllSuffix in case it is needed
function applyFetchAll(config, url) {
    // if we pass fetchAll means we want the full log -> start=0 will trigger that on the server
    if (config.fetchAll && !url.includes(fetchAllSuffix)) {
        return `${url}${fetchAllSuffix}`;
    }
    return url;
}

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

/**
 * Constructs an escaped url based on the arguments, with forward slashes between them
 * e.g. buildURL('organizations', orgName, 'runs', runId) => organizations/my%20org/runs/34
 */
export function buildUrl(...args) {
    let url = '';
    for (let i = 0; i < args.length; i++) {
        if (i > 0) {
            url += '/';
        }
        url += encodeURIComponent(args[i]);
    }
    return url;
}

/**
 * Returns a relative URL based on the current location
 */
export function relativeUrl(location, ...args) {
    return ensureTrailingSlash(location.pathname) + buildUrl.apply(null, args);
}
