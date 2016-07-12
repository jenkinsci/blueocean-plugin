/**
 * Build a root-relative URL to the organization's pipeline list screen.
 * @param organization
 */
export const buildOrganizationUrl = (organization) =>
    `/organizations/${encodeURIComponent(organization)}`;

/**
 * Build a root-relative URL to the pipeline details screen.
 * @param organization
 * @param fullName
 * @param tabName
 * @returns {string}
 */
export const buildPipelineUrl = (organization, fullName, tabName) => {
    const baseUrl = `/organizations/${encodeURIComponent(organization)}/` +
        `${encodeURIComponent(fullName)}`;

    return tabName ? `${baseUrl}/${tabName}` : baseUrl;
};

/**
 * Build a root-relative URL to the run details screen.
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
        `pipelines/${name}`;
    if (isMultiBranch) {
        return `${baseUrl}/branches/${uriString(branch)}/runs/${runId}/nodes/`;
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
    const { name, runId, branch, _appURLBase, isMultiBranch, node } = config;
    let baseUrl =
        `${_appURLBase}/rest/organizations/jenkins/` +
        `pipelines/${name}`;
    if (isMultiBranch) {
        baseUrl = `${baseUrl}/branches/${uriString(branch)}`;
    }
    if (node && node !== null) {
        return `${baseUrl}/runs/${runId}/nodes/${node}/steps`;
    }
    return `${baseUrl}/runs/${runId}/steps/`;
}

/*
 * helper to calculate general log url, includes filename.
 * If we have multibranch we generate a slightly different url
 * @param config { name, runId, branch, _appURLBase, isMultiBranch}
 */
export function calculateRunLogURLObject(config) {
    const { name, runId, branch, _appURLBase, isMultiBranch } = config;
    const baseUrl = `${_appURLBase}/rest/organizations/jenkins` +
        `/pipelines/${name}`;
    let url;
    let fileName;
    if (isMultiBranch) {
        url = `${baseUrl}/branches/${uriString(branch)}/runs/${runId}/log/`;
        fileName = `${branch}-${runId}.txt`;
    } else {
        url = `${baseUrl}/runs/${runId}/log/`;
        fileName = `${runId}.txt`;
    }
    return {
        url,
        fileName,
    };
}

/**
 * Constructs an escaped url based on the arguments, with forward slashes between them
 * e.g. buildURL('organizations', orgName, 'runs', runId) => organizations/my%20org/runs/34
 */
export const buildUrl = (...args) => {
    let url = '';
    for (let i = 0; i < args.length; i++) {
        if (i > 0) {
            url += '/';
        }
        url += encodeURIComponent(args[i]);
    }
    return url;
};
