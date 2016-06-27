/**
 * Trim the last path segment off a URL and return it.
 * Handles trailing slashes nicely.
 * @param url
 * @returns {string}
 */
export const removeLastUrlSegment = (url) => {
    const paths = url.split('/').filter(path => (path.length > 0));
    paths.pop();
    return paths.join('/');
};

/**
 * Build a root-relative URL to the run details modal.
 * @param organization
 * @param pipeline
 * @param branch
 * @param runId
 * @param tabName
 */
export const buildRunDetailsUrl = (organization, pipeline, branch, runId, tabName) => {
    const baseUrl = `/organizations/${encodeURIComponent(organization)}/` +
        `${encodeURIComponent(pipeline)}/detail/` +
        `${encodeURIComponent(branch)}/${encodeURIComponent(runId)}`;
    return tabName ? `${baseUrl}/${tabName}` : baseUrl;
};

/*
 * helper to clean the path replace(/%2F/g, '%252F')
 * @param input
 */
export const uriString = (input) => encodeURIComponent(input).replace(/%2F/g, '%252F');

/*
 * helper to calculate log url. When we have a node we get create a special url, otherwise we use the url passed to us
 * @param config { nodesBaseUrl, node, url}
 */
export const calculateLogUrl = (config) => {
    if (config.node) {
        const { nodesBaseUrl, node } = config;
        return `${nodesBaseUrl}/${node.id}/log`;
    }
    return config.url;
};

/*
 * helper to calculate node(flowNodes) url.
 * If we have multibranch we generate a slightly different url
 * @param config { name, runId, branch, _appURLBase, isMultiBranch}
 */
export function calculateNodeBaseUrl(config) {
    const { name, runId, branch, _appURLBase, isMultiBranch } = config;
    const baseUrl =
        `${_appURLBase}/rest/organizations/jenkins/` +
        `pipelines/${uriString(name)}`;
    if (isMultiBranch) {
        return `${baseUrl}/branches/${uriString(branch)}/runs/${uriString(runId)}/nodes/`;
    }
    return `${baseUrl}/runs/${uriString(runId)}/nodes/`;
}

/*
 * helper to calculate steps(flowNodes) url.
 * If we have multibranch we generate a slightly different url
 * and if there are node in we request the steps for this node
 * @param config { name, runId, branch, _appURLBase, isMultiBranch, node}
 */
export function calculateStepsBaseUrl(config) {
    const { name, runId, branch, _appURLBase, isMultiBranch, node } = config;
    let baseUrl =
        `${_appURLBase}/rest/organizations/jenkins/` +
        `pipelines/${uriString(name)}`;
    if (isMultiBranch) {
        baseUrl = `${baseUrl}/branches/${uriString(branch)}`;
    }
    if (node && node !== null) {
        return `${baseUrl}/runs/${uriString(runId)}/nodes/${uriString(node)}/steps`;
    }
    return `${baseUrl}/runs/${uriString(runId)}/steps/`;
}


/*
 * helper to calculate general log url, includes filename.
 * If we have multibranch we generate a slightly different url
 * @param config { name, runId, branch, _appURLBase, isMultiBranch}
 */
export function calculateRunLogURLObject(config) {
    const { name, runId, branch, _appURLBase, isMultiBranch } = config;
    const baseUrl = `${_appURLBase}/rest/organizations/jenkins` +
        `/pipelines/${uriString(name)}`;
    let url;
    let fileName;
    if (isMultiBranch) {
        url = `${baseUrl}/branches/${uriString(branch)}/runs/${uriString(runId)}/log/`;
        fileName = `${branch}-${runId}.txt`;
    } else {
        url = `${baseUrl}/runs/${uriString(runId)}/log/`;
        fileName = `${runId}.txt`;
    }
    return {
        url,
        fileName,
    };
}
