/**
 * THIS IS NOT A TEST SUITE !!
 *
 * This module performs some initialization of the Extension Store.
 *
 * TODO: Find a nicer way of doing this. See below.
 *
 * One thing we could do is get the js-extension plugin for js-builder to
 * read the plugins target/classes/jenkins-js-extension.json file and use it
 * to initialize the store. That's nice in that we can automatically do it,
 * but the problem is that it wouldn't actually work in this case because we
 * also need to specify the blueocean-web module. This initalization is needed
 * for i18n components that need access to plugin version info, which comes
 * from the ExtensionStore.
 */

import { store, classMetadataStore } from '@jenkins-cd/js-extensions';

export function mockExtensionsForI18n() {
    store.init({
        extensionData: [
            {
                "extensions": [],
                "hpiPluginId": "blueocean-dashboard",
                "hpiPluginVer": "1.1"
            },
            {
                "extensions": [],
                "hpiPluginId": "blueocean-web",
                "hpiPluginVer": "1.1"
            }
        ],
        classMetadataStore: classMetadataStore
    });
}
