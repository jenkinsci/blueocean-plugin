//
// General "system" config information.
//
// TODO: This should be in a general sharable component.
// Passing it around in the react context is silly.
//

exports.blueoceanAppURL = '/';
exports.jenkinsRootURL = '';

exports.loadConfig = function () {
    try {
        const headElement = document.getElementsByTagName('head')[0];

        // Look up where the Blue Ocean app is hosted
        exports.blueoceanAppURL = headElement.getAttribute('data-appurl');

        if (typeof exports.blueoceanAppURL !== 'string') {
            exports.blueoceanAppURL = '/';
        }

        exports.jenkinsRootURL = headElement.getAttribute('data-rooturl');
    } catch (error) {
        // eslint-disable-next-line no-console
        console.warn('error reading attributes from document; urls will be empty');
    }
};
