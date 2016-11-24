import { Fetch } from '@jenkins-cd/blueocean-core-js';

exports.initialize = function (oncomplete) {
    // Get the extension list metadata from Jenkins.
    // Might want to do some flux fancy-pants stuff for this.
    const appRoot = document.getElementsByTagName("head")[0].getAttribute("data-appurl");
    const Extensions = require('@jenkins-cd/js-extensions');
    Extensions.init({
        extensionDataProvider: cb => Fetch.fetchJSON(`${appRoot}/js-extensions`).then(body => cb(body.data)).catch(Fetch.consoleError),
        classMetadataProvider: (type, cb) => Fetch.fetchJSON(`${appRoot}/rest/classes/${type}/`).then(cb).catch(Fetch.consoleError)
    });

    Extensions.store.loadExtensionData(oncomplete);
};
