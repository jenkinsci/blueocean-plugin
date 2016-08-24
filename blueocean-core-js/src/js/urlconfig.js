let jenkinsRootURL = null;
let blueoceanAppURL = null;

// General "system" config information.
//
// TODO: This should be in a general sharable component.
// Passing it around in the react context is silly.
//
function loadConfig() {
    const headElement = document.getElementsByTagName('head')[0];

    // Look up where the Blue Ocean app is hosted
    blueoceanAppURL = headElement.getAttribute('data-appurl');

    if (typeof blueoceanAppURL !== 'string') {
        blueoceanAppURL = '/';
    }

    jenkinsRootURL = headElement.getAttribute('data-rooturl');
}

export default {
    getJenkinsRootURL() {
        if (!jenkinsRootURL) {
            loadConfig();
        }
        return jenkinsRootURL;
    },
    
    getBlueAppUrl() {
        if (!blueoceanAppURL) {
            loadConfig();
        }
        return blueoceanAppURL;
    },
};
