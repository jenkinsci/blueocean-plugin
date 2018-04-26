import * as modules from '@jenkins-cd/js-modules';

/**
 * Executes extension init and hack for blueocean-web i18n
 */
export function execute(done, config) {
    // Get the extension list metadata from Jenkins.
    // have a special handling for core-js, must load the web bundle...
    const Extensions = require('@jenkins-cd/js-extensions');
    const appRoot = document.getElementsByTagName('head')[0].getAttribute('data-appurl');

    Extensions.init({
        extensionData: window.$blueocean.jsExtensions,
        classMetadataProvider: (type, cb) => {
            const fetch = require('./fetch').Fetch;
            fetch
                .fetchJSON(`${appRoot}/rest/classes/${type}/`)
                .then(cb)
                .catch(fetch.consoleError);
        },
    });
    require('./i18n/bundle-startup').execute(done, { hpiPluginId: 'blueocean-web' });
}
