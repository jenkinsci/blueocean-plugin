// this function is executed before all other code during bundle's onStartup
// see gulpfile, builder.bundle().onStartup()

import Errors from './Errors';

export function execute(done, bundleConfig) {
    Errors.initializeErrorHandling();

    // Get the extension list metadata from Jenkins.
    // Might want to do some flux fancy-pants stuff for this.
    const appRoot = document.getElementsByTagName("head")[0].getAttribute("data-appurl");
    const Extensions = require('@jenkins-cd/js-extensions');

    Extensions.init({
        extensionData: window.$blueocean.jsExtensions,
        classMetadataProvider: (type, cb) => {
            const fetch = require('@jenkins-cd/blueocean-core-js/dist/js/fetch');
            fetch.Fetch.fetchJSON(`${appRoot}/rest/classes/${type}/`).then(cb).catch(fetch.Fetch.consoleError);
        }
    });

    // Bootstrap the i18n resources too...
    const i18nBootstrap = require('@jenkins-cd/blueocean-core-js/dist/js/i18n/bundle-startup');
    i18nBootstrap.execute(done, bundleConfig);
}
