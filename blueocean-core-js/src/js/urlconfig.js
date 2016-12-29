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
        console.warn('error reading attributes from document; urls will be empty', error);

        loaded = false;
    }
}

export default {
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
