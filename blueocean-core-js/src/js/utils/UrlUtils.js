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
 * Examines the provided object for:
 * organization, pipeline, branch, runId
 * and builds a path to the thing as best it can...
 */
export function getRestUrl({ organization, pipeline, branch, runId }) {
    // TODO: Check usages, see if this is where it should be and named sensibly
    const pipelineName = typeof pipeline === 'object' ? pipeline.fullName : pipeline;
    const organizationName = organization || (typeof pipeline === 'object' ? pipeline.organization : '');
    const jenkinsUrl = AppConfig.getJenkinsRootURL();
    let url = `${jenkinsUrl}/blue/rest/organizations/${encodeURIComponent(organizationName)}`;
    if (pipelineName) {
        // pipelineName might include a folder path, don't encode it
        url += `/pipelines/${pipelineName}`;
    }
    if (branch) {
        // JENKINS-37712 branch needs to be double-encoded for some reason
        url += `/branches/${encodeURIComponent(encodeURIComponent(branch))}`;
    }
    if (runId) {
        url += `/runs/${encodeURIComponent(runId)}`;
    }
    return ensureTrailingSlash(url);
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
