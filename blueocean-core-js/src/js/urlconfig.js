let jenkinsRootURL = '';
let blueOceanAppURL = '/';
let restBaseURL = '';

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
