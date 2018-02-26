/*
 * The MIT License
 *
 * Copyright (c) 2017, CloudBees, Inc.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

/**
 * Client bundle startup script for loading i18n resources.
 * <p>
 * Ensures that i18n resources are loaded before the bundle starts to execute.
 */

import config from '../config';
import logging from '../logging';
import i18nTranslator from './i18n';

const logger = logging.logger('io.jenkins.blueocean.i18n.startup');

/**
 * Load the i18n resources defined for the plugin (in the extensions).
 *
 * @param {function} done Callback function. Called once all resources.
 * @param {object} bundleConfig Bundle configuration parameters.
 */
export function execute(done, bundleConfig) {
    if (bundleConfig.hpiPluginId) {
        const pluginInfo = config.getPluginInfo(bundleConfig.hpiPluginId);
        if (pluginInfo) {
            if (pluginInfo.i18nBundles && pluginInfo.i18nBundles.length > 0) {
                logger.debug(`Plugin ${bundleConfig.hpiPluginId} defines i18n resource bundles that must be loaded:`, pluginInfo.i18nBundles);
                const loadedBundles = [];
                const loadBundle = (namespace) => {
                    let hpiPluginId = bundleConfig.hpiPluginId;
                    let i18nResource = namespace;

                    // By default the i18n resource is loaded from the same plugin as the
                    // js-extension bundle, but sometimes you need to load it from another plugin.
                    // The namespace can be defined as an object defining an alternate hpiPluginId.
                    if (typeof namespace === 'object') {
                        hpiPluginId = namespace.hpiPluginId;
                        i18nResource = namespace.resource;
                    }

                    const translator = i18nTranslator(hpiPluginId, i18nResource, () => {
                        if (loadedBundles.indexOf(namespace) === -1) {
                            logger.debug(`Loading of i18n resource bundle "${bundleConfig.hpiPluginId}:${namespace}" done.`);
                            loadedBundles.push(namespace);
                            if (loadedBundles.length === pluginInfo.i18nBundles.length) {
                                // All bundles are loaded ... ok for the bundle to execute now (from an i18n pov).
                                logger.log(`All i18n "${bundleConfig.hpiPluginId}" resource bundles loaded.`, pluginInfo.i18nBundles);
                                done();
                            }
                        }
                    });
                    // Call the translator to trigger loading.
                    // Any random key is fine ... just needs to trigger the loading.
                    translator('xxxx');
                };
                pluginInfo.i18nBundles.forEach((bundleNamespace) => loadBundle(bundleNamespace));
            } else {
                logger.debug(`Plugin "${bundleConfig.hpiPluginId}" doesn't define any i18n resource bundles.`);
                done();
            }
        } else {
            logger.warn(`Unexpected error finding plugin info for plugin ${bundleConfig.hpiPluginId}. There should be a preloaded jsExtensions entry.`);
            done();
        }
    } else {
        done();
    }
}
