import AppConfig from '../config';

/**
 * Gives classic jenkins job path prefix.
 * For organization group '/folder1/org1', job prefix is: /job/folder1/job/org1
 * For root organization group '/', there is no prefix: ''.
 * @param organizationGroupName organization group
 * @returns {string}
 */
export function classicOrganizationRoot(organizationGroupName) {
    // TODO: Move to UrlBuilder
    if (organizationGroupName && organizationGroupName !== '/') {
        return `${organizationGroupName.split('/').join('/job/')}`;
    }
    return '';
}

/**
 * Double encode name, feature/test#1 is encoded as feature%252Ftest%25231
 */
export const doubleUriEncode = input => encodeURIComponent(encodeURIComponent(input));

// general fetchAllTrigger
export const fetchAllSuffix = '?start=0';

// Add fetchAllSuffix in case it is needed
export function applyFetchAll(config, url) {
    // if we pass fetchAll means we want the full log -> start=0 will trigger that on the server
    if (config.fetchAll && !url.includes(fetchAllSuffix)) {
        return `${url}${fetchAllSuffix}`;
    }
    return url;
}

// using the hook 'location.search'.includes('start=0') to trigger fetchAll
export function calculateFetchAll(props) {
    const { location: { search } } = props;

    if (search) {
        const stepReg = /start=([0-9]{1,})/;
        const match = stepReg.exec(search);
        if (match && match[1] && Number(match[1]) === 0) {
            return true;
        }
    }
    return false;
}

// using the hook 'location.search'.includes('view=0') to trigger the logConsole view instead of steps
export const calculateLogView = function(props) {
    // TODO: find usages, change name to something sensible
    const { location: { search } } = props;

    if (search) {
        const viewReg = /view=([0-9]{1,})/;
        const match = viewReg.exec(search);
        if (match && match[1] && Number(match[1]) === 0) {
            return true;
        }
    }
    return false;
};

/*
 * helper to calculate log url. When we have a node we get create a special url, otherwise we use the url passed to us
 * @param config { nodesBaseUrl, node, url}
 */
export const calculateLogUrl = config => {
    // TODO: Check usages, see if this is where it should be and named sensibly
    let returnUrl = config.url;
    if (config.node) {
        const { nodesBaseUrl, node } = config;
        returnUrl = `${nodesBaseUrl}/${node.id}/log/`;
    }
    return applyFetchAll(config, returnUrl);
};

/*
 * helper to calculate node(flowNodes) url.
 * If we have multibranch we generate a slightly different url
 * @param config { name, runId, branch, _appURLBase, isMultiBranch}
 */
export function calculateNodeBaseUrl(config) {
    // TODO: Check usages, see if this is where it should be and named sensibly
    const { name, runId, branch, _appURLBase, isMultiBranch, organization } = config;
    const baseUrl = `${_appURLBase}/rest/organizations/${encodeURIComponent(organization)}/` + `pipelines/${name}`;
    if (isMultiBranch) {
        return `${baseUrl}/branches/${doubleUriEncode(branch)}/runs/${runId}/nodes/`;
    }
    return `${baseUrl}/runs/${runId}/nodes/`;
}

/*
 * helper to calculate steps(flowNodes) url.
 * If we have multibranch we generate a slightly different url
 * and if there are node in we request the steps for this node
 * @param config { name, runId, branch, _appURLBase, isMultiBranch, node}
 */
export function calculateStepsBaseUrl(config) {
    // TODO: Check usages, see if this is where it should be and named sensibly
    const { name, runId, branch, _appURLBase, isMultiBranch, node, organization } = config;
    let baseUrl = `${_appURLBase}/rest/organizations/${encodeURIComponent(organization)}/` + `pipelines/${name}`;
    if (isMultiBranch) {
        baseUrl = `${baseUrl}/branches/${doubleUriEncode(branch)}`;
    }
    if (node && node !== null) {
        return `${baseUrl}/runs/${runId}/nodes/${node}/steps/`;
    }
    return `${baseUrl}/runs/${runId}/steps/`;
}
/*
 * helper to calculate general log url, includes filename.
 * If we have multibranch we generate a slightly different url
 * @param config { name, runId, branch, _appURLBase, isMultiBranch}
 */
export function calculateRunLogURLObject(config) {
    // TODO: Check usages, see if this is where it should be and named sensibly
    const { name, runId, branch, _appURLBase, isMultiBranch, organization } = config;
    const baseUrl = `${_appURLBase}/rest/organizations/${encodeURIComponent(organization)}` + `/pipelines/${name}`;
    let url;
    let fileName;
    if (isMultiBranch) {
        url = `${baseUrl}/branches/${doubleUriEncode(branch)}/runs/${runId}/log/`;
        fileName = `${branch}-${runId}.txt`;
    } else {
        url = `${baseUrl}/runs/${runId}/log/`;
        fileName = `${runId}.txt`;
    }
    url = applyFetchAll(config, url);
    return {
        url,
        fileName,
    };
}

/**
 * Provide a pagination function for the generic
 * blueocean pagination
 */
export function paginateUrl(url) {
    const sep = url.indexOf('?') >= 0 ? '&' : '?';
    return (start, limit) => `${url}${sep}start=${start}&limit=${limit}`;
}

/**
 * Returns a new string which ends with a slash, or the
 * original if it already does
 */
export function endSlash(str) {
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
    return endSlash(url);
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
    return endSlash(location.pathname) + buildUrl.apply(null, args);
}
