let jenkinsRootURL = '';
let blueOceanAppURL = '/';
let restBaseURL = '';
let crumbToken = '';
let crumbHeaderName = '';

let loaded = false;

function loadConfig() {
    try {
        const headElement = document.getElementsByTagName('head')[0];

        // typically '/jenkins/'
        jenkinsRootURL = headElement.getAttribute('data-rooturl');
        if (typeof jenkinsRootURL !== 'string') {
            jenkinsRootURL = '/';
        }

        // typically '/jenkins/blue'
        blueOceanAppURL = headElement.getAttribute('data-appurl');
        if (typeof blueOceanAppURL !== 'string') {
            blueOceanAppURL = '/';
        }

        // typically '/jenkins/blue/rest'
        restBaseURL = `${blueOceanAppURL}/rest`.replace(/\/\/+/g, '/'); // eliminate any duplicated slashes

        // load crumb token used for POST requests
        crumbToken = headElement.getAttribute('data-crumbtoken');
        if (typeof crumbToken !== 'string') {
            crumbToken = '';
        }

        // load crumb header name used for POST requests
        crumbHeaderName = headElement.getAttribute('data-crumbtoken-field');
        if (typeof crumbHeaderName !== 'string') {
            crumbHeaderName = '';
        }

        loaded = true;
    } catch (error) {
        // eslint-disable-next-line no-console
        console.warn('error reading attributes from document; urls will be empty', error);

        loaded = false;
    }
}

export const UrlConfig = {
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

    getCrumbHeaderName() {
        if (!loaded) {
            loadConfig();
        }
        return crumbHeaderName;
    },

    getCrumbToken() {
        if (!loaded) {
            loadConfig();
        }
        return crumbToken;
    },

    getRestBaseURL() {
        if (!loaded) {
            loadConfig();
        }
        return restBaseURL;
    },
    // for testing purposes: allow url's to be reloaded
    enableReload() {
        loaded = false;
    },
};
