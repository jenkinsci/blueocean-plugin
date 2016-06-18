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
 * Build a root-relative URL to the pipeline details screen.
 * @param organization
 * @param fullName
 * @param tabName
 * @returns {string}
 */
export const buildPipelineUrl = (organization, fullName, tabName) => {
    const pathElements = fullName.split('/');
    const pipeline = pathElements.pop();
    const folderPath = pathElements.join('/');
    const folderPart = folderPath ? `${encodeURIComponent(folderPath)}/` : '';

    const baseUrl = `/organizations/${encodeURIComponent(organization)}/` +
        `${folderPart}` + `${encodeURIComponent(pipeline)}`;

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
