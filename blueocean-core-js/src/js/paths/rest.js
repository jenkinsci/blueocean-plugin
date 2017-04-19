import AppConfig from '../config';

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

    organizationPipelines(organizationName) {
        return `${this.apiRoot()}/search/?q=type:pipeline;organization:${encodeURIComponent(organizationName)};excludedFromFlattening:jenkins.branch.MultiBranchProject,hudson.matrix.MatrixProject&filter=no-folders`;
    },

    allPipelines() {
        return `${this.apiRoot()}/search/?q=type:pipeline;organization:${AppConfig.getOrganizationName()};excludedFromFlattening:jenkins.branch.MultiBranchProject,hudson.matrix.MatrixProject&filter=no-folders`;
    },

    activities(organization, pipeline, branch) {
        const branchStr = branch ? `?branch=${branch}` : '';
        return `${this.apiRoot()}/organizations/${encodeURIComponent(organization)}/pipelines/${pipeline}/activities/${branchStr}`;
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

    queuedItem(organization, pipeline, queueId) {
        return `${this.pipeline(organization, pipeline)}queue/${queueId}/`;
    },
};
