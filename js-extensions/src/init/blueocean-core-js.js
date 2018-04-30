import * as modules from '@jenkins-cd/js-modules';

/**
 * Executes extension init and hack for blueocean-web i18n
 */
export function execute(done, config) {
    // Get the extension list metadata from Jenkins.
    // have a special handling for core-js, must load the web bundle...
    const Extensions = require('@jenkins-cd/js-extensions');
    const appRoot = document.getElementsByTagName('head')[0].getAttribute('data-appurl');
    const CoreJs = modules.requireModule('jenkins-cd-blueocean-core-js:jenkins-cd-blueocean-core-js@any');

    Extensions.init({
        extensionData: window.$blueocean.jsExtensions,
        classMetadataProvider: (type, cb) => {
            const fetch = CoreJs.Fetch;
            fetch
                .fetchJSON(`${appRoot}/rest/classes/${type}/`)
                .then(cb)
                .catch(fetch.consoleError);
        },
    });
    CoreJs.i18nBundleStartup(done, { hpiPluginId: 'blueocean-web' });
}
