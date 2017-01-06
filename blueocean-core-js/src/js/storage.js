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
 * Client-side local storage for Blue Ocean.
 * See https://tfennelly.github.io/jenkins-js-storage/
 */

import * as storage from '@jenkins-cd/storage';
import { blueocean } from './scopes';
import config from './config';

export const jenkinsNS = storage.jenkinsNamespace();
export const installInfo = jenkinsNS.subspace('installInfo');

/**
 * Check do we need to clear the jenkinsNS.
 * <p>
 * Simple process of checking if the version OR list of plugins
 * stored (from the last change) have changed based on what was
 * delivered with this page.
 * <p>
 * Internal use only. Exported for testing purposes only.
 * @param {string} installVersion The version of the Jenkins instance that's running now
 * i.e. that loaded this page.
 * @param {Array} installPluginList The list of active plugins installed in the Jenkins instance
 * that's running now i.e. that loaded this page.
 * @private
 */
export const _clearJenkinsNS = (installVersion, installPluginList) => {
    // Info about the Jenkins that was running the last time we loaded this page.
    const storedVersion = installInfo.get('version');
    const storedPluginList = installInfo.get('plugins');

    const doClear = (because) => {
        jenkinsNS.clear();
        installInfo.set('version', installVersion);
        installInfo.set('plugins', installPluginList);
        installInfo.set('lastcleared', {
            at: Date.now(),
            because,
        });
    };

    try {
        if (storedVersion && storedPluginList) {
            // compare the Jenkins version
            if (installVersion !== storedVersion) {
                doClear(`Jenkins versions did not match. installVersion: ${installVersion}, storedVersion: ${storedVersion}`);
            } else {
                // compare the plugin lists
                if (installPluginList.length !== storedPluginList.length) {
                    // Different number of active plugins.
                    // No need to check the names and versions.
                    doClear('Different number of active plugins');
                } else {
                    // Same number of plugins. Lets check that they all
                    // match up i.e. that we can find each plugin in each list and
                    // that the versions match.
                    try {
                        installPluginList.forEach((installedPlugin) => {
                            let found = false;
                            storedPluginList.forEach((storedPlugin) => {
                                if (storedPlugin.hpiPluginId === installedPlugin.hpiPluginId) {
                                    // same plugin.
                                    found = true;
                                    // Check the versions.
                                    if (storedPlugin.hpiPluginVer !== installedPlugin.hpiPluginVer) {
                                        throw new Error(`Different plugin versions for plugin ${installedPlugin.hpiPluginId}`);
                                    }
                                }
                            });
                            if (!found) {
                                throw new Error(`New plugin installed ${installedPlugin.hpiPluginId}`);
                            }
                        });
                    } catch (e) {
                        // One of the plugins has been updated or removed.
                        // See Errors thrown inside above try/catch.
                        doClear(e.message);
                    }
                }
            }
        } else {
            // Theoretically no need to clear in this case,
            // but lets do it anyway.
            doClear('No Jenkins info stored. Clearing anyway, just in case.');
        }
    } catch (e) {
        console.error('Unexpected error while checking/clearing Jenkins instance client-side storage. Clearing as a precaution.', e);
        doClear(`Unexpected error while checking/clearing Jenkins instance client-side storage: ${e.message}`);
    }
};

// Call the clear function automatically.
const installVersion = config.getJenkinsConfig().version;
const installPluginList = blueocean.jsExtensions;
if (installVersion && installPluginList) {
    _clearJenkinsNS(installVersion, installPluginList);
} else {
    console.warn('Unexpected state. Blue Ocean preload state not on page as expected. This is okay if running in a test.');
}
