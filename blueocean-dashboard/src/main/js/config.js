
//
// General "system" config information.
//
// TODO: This should be in a general sharable component.
// Passing it around in the react context is silly.
//

exports.loadConfig = function () {
    const headElement = document.getElementsByTagName('head')[0];

    // Look up where the Blue Ocean app is hosted
    exports.blueoceanAppURL = headElement.getAttribute('data-appurl');

    if (typeof exports.blueoceanAppURL !== 'string') {
        exports.blueoceanAppURL = '/';
    }

    exports.jenkinsRootURL = headElement.getAttribute('data-rooturl');
};

exports.getJenkinsRootURL = function getJenkinsRootURL() {
    if (!exports.jenkinsRootURL) {
        exports.loadConfig();
    }
    return exports.jenkinsRootURL;
};

exports.getRestRoot = function getRestRoot() {
    return `${exports.getJenkinsRootURL()}/blue/rest`;
};
