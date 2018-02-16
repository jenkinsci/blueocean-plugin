/**
 * This object defines rest paths
 */
export default {
    _convertSlashes(pipeline) {
        return pipeline.replace(/\//g, '/pipelines/');
    },

    apiRoot() {
        return '/blue/rest';
    },

    pipelines(organizationName, searchText) {
        const organization = organizationName ? `;organization:${encodeURIComponent(organizationName)}` : '';
        let searchTextQuery = '';

        if (searchText) {
            searchTextQuery = ('*' + searchText + '*').replace(/\//g, '*/*').replace('**', '*');
        }

        return `${this.apiRoot()}/search/?q=type:pipeline${organization};pipeline:${encodeURIComponent(searchTextQuery)};excludedFromFlattening:jenkins.branch.MultiBranchProject,hudson.matrix.MatrixProject&filter=no-folders`;
    },

    runs(organization, pipeline, branch) {
        const branchStr = branch ? `?branch=${branch}` : '';
        return `${this.apiRoot()}/organizations/${encodeURIComponent(organization)}/pipelines/${pipeline}/runs/${branchStr}`;
    },

    run({ organization, pipeline, branch, runId }) {
        if (branch) {
            return `${this.pipeline(organization, pipeline)}branches/${encodeURIComponent(encodeURIComponent(branch))}/runs/${runId}/`;
        }

        return `${this.pipeline(organization, pipeline)}runs/${runId}/`;
    },

    pipeline(organization, pipeline) {
        return `${this.apiRoot()}/organizations/${encodeURIComponent(organization)}/pipelines/${this._convertSlashes(pipeline)}/`;
    },
    branches(organization, pipeline) {
        return `${this.apiRoot()}/organizations/${encodeURIComponent(organization)}/pipelines/${pipeline}/branches/?filter=origin`;
    },

    pullRequests(organization, pipeline) {
        return `${this.apiRoot()}/organizations/${encodeURIComponent(organization)}/pipelines/${pipeline}/branches/?filter=pull-requests`;
    },
};
