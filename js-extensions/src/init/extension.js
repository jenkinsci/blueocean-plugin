import * as modules from '@jenkins-cd/js-modules';

/**
 * Executes i18n for non-core-js modules
 */
export function execute(done, config) {
    const CoreJs = modules.requireModule('jenkins-cd-blueocean-core-js:jenkins-cd-blueocean-core-js@any');
    if (CoreJs.i18nBundleStartup) {
        CoreJs.i18nBundleStartup(done, config);
    } else {
        // For pre-1.4 compatibility
        require('@jenkins-cd/blueocean-core-js/dist/js/i18n/bundle-startup').execute(done, config);
    }
}
