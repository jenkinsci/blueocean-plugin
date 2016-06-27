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
