import * as modules from '@jenkins-cd/js-modules';

/**
 * Executes i18n for non-core-js modules
 */
export function execute(done, config) {
	const CoreJs = modules.requireModule('jenkins-cd-blueocean-core-js:jenkins-cd-blueocean-core-js@any');
	CoreJs.i18nBundleStartup(done, config);
};
