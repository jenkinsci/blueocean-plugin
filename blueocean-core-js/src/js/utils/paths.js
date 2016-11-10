let blueOceanAppURL = '/';
let jenkinsRootURL = '';

let loaded = false;

function loadConfig() {
    try {
        const headElement = document.getElementsByTagName('head')[0];

        // Look up where the Blue Ocean app is hosted
        blueOceanAppURL = headElement.getAttribute('data-appurl');
        if (typeof blueOceanAppURL !== 'string') {
            blueOceanAppURL = '/';
        }
          
        jenkinsRootURL = headElement.getAttribute('data-rooturl');
        loaded = true;
    } catch (error) {
        // eslint-disable-next-line no-console
        console.warn('error reading attributes from document; urls will be empty');

        loaded = false;
    }
}

export const AppPaths = {
    getJenkinsRootURL() {
        if (!loaded) {
            loadConfig();
        }
        return jenkinsRootURL;
    },
    
    getBlueOceanAppURL() {
        if (!loaded) {
            loadConfig();
        }
        
        return blueOceanAppURL;
    },
};

export const RestPaths = {
    apiRoot() {
        return '/blue/rest';
    },

    organizationPipelines(organizationName) {
        return `${RestPaths.apiRoot()}/search/?q=type:pipeline;organization:${encodeURIComponent(organizationName)};excludedFromFlattening:jenkins.branch.MultiBranchProject,hudson.matrix.MatrixProject&filter=no-folders`;
    },

    allPipelines() {
        return `${RestPaths.apiRoot()}/search/?q=type:pipeline;excludedFromFlattening:jenkins.branch.MultiBranchProject,hudson.matrix.MatrixProject&filter=no-folders`;
    },

    activities(organization, pipeline) {
        return `${RestPaths.apiRoot()}/organizations/${organization}/pipelines/${pipeline}/activities/`;
    },

    run({ organization, pipeline, branch, runId }) {
        if (branch) {
            return `${RestPaths.pipeline(organization, pipeline)}branches/${branch}/runs/${runId}/`;
        }

        return `${RestPaths.pipeline(organization, pipeline)}runs/${runId}/`;
    },
    
    pipeline(organization, pipeline) {
        return `${RestPaths.apiRoot()}/organizations/${encodeURIComponent(organization)}/pipelines/${pipeline}/`;
    },
    branches(organization, pipeline) {
        return `${RestPaths.apiRoot()}/organizations/${encodeURIComponent(organization)}/pipelines/${pipeline}/branches/?filter=origin`;  
    },

    pullRequests(organization, pipeline) {
        return `${RestPaths.apiRoot()}/organizations/${encodeURIComponent(organization)}/pipelines/${pipeline}/branches/?filter=pull-requests`;
    },  

    queuedItem(organization, pipeline, queueId) {
        return `${RestPaths.pipeline(organization, pipeline)}queue/${queueId}/`;
    },
};
