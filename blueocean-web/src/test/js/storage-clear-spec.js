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

import { assert } from 'chai';
import { jenkinsNS, _clearJenkinsNS } from '../../src/js/storage';

/**
 * Tests to make sure that the Jenkins StorageNamespace gets cleared out
 * (or not) appropriately i.e. when the Jenkins version changes, or when the
 * active plugin state changes (installs, updates, removals etc).
 */

describe('Storage Clearing', () => {
    const installInfo = jenkinsNS.subspace('installInfo');

    // A subspace for store some random things that should get
    // wiped when the state of the server changes.
    const magicNamespace = jenkinsNS.subspace('magicNamespace');

    function version() {
        return installInfo.get('version');
    }
    function plugins() {
        return installInfo.get('plugins');
    }
    function lastcleared() {
        return (installInfo.get('lastcleared') || { because: '??? lastcleared has not been set !!!' });
    }

    // Clear the state in case
    jenkinsNS.clear();

    /**
     * Check that the currently stored Jenkins state is the same as
     * the supplied state object.
     * @param {object} stateObject The state object e.g. see t1State.
     */
    function assertState(stateObject) {
        assert.equal(version(), stateObject.version, `Jenkins versions do not match. Last cleared because: "${lastcleared().because}"`);
        assert.deepEqual(plugins(), stateObject.plugins, `Jenkins active plugins do not match. Last cleared because: "${lastcleared().because}"`);
    }

    function doClearCall(stateObject) {
        _clearJenkinsNS(stateObject.version, stateObject.plugins);
        assertState(stateObject);
    }

    function setMagicVal() {
        magicNamespace.set('a', 'aval');
    }
    function assertMagicValIsSet() {
        if (magicNamespace.get('a') !== 'aval') {
            assert.fail(magicNamespace.get('a'), 'aval', `magicNamespace subspace has been cleared unexpectedly. Last cleared because: ${lastcleared().because}`);
        }
    }
    function assertMagicValIsNotSet() {
        if (magicNamespace.get('a')) {
            assert.fail(magicNamespace.get('a'), undefined, `magicNamespace subspace has not been cleared out as expected. Last cleared because: ${lastcleared().because}`);
        }
    }
    function assertLastClearBecause(because) {
        assert.equal(lastcleared().because, because, 'lastcleared string not as expected.');
    }

    const t1State = {
        version: '2.32.1',
        plugins: [
            { hpiPluginId: 'pluginA', hpiPluginVer: '1.0' },
            { hpiPluginId: 'pluginB', hpiPluginVer: '1.0' },
        ],
    };

    it('T1: 1st run in browser', () => {
        // First run of course nothing is stored in the client-side
        // storage, so the version should NOT be set.
        assert.isUndefined(version());

        doClearCall(t1State);
        assertLastClearBecause('No Jenkins info stored. Clearing anyway, just in case.');
    });

    it('T2: 2nd run in browser - nothing changed on the server side => stored values the same', () => {
        // store something in the magicNamespace
        setMagicVal();
        assertMagicValIsSet();

        // Call _clearJenkinsNS with the same state values as with T1
        doClearCall(t1State);
        assertLastClearBecause('No Jenkins info stored. Clearing anyway, just in case.');

        // The earlier value stored in the magicNamespace subspace should
        // still be there, indicating the StorageNamespace was not cleared (as expected).
        // See above.
        assertMagicValIsSet();
    });

    const t3State = {
        version: '2.33.0', // New Jenkins version
        plugins: t1State.plugins,
    };

    it('T3: 3rd run in browser - new Jenkins version => should trigger state clearout', () => {
        // Call _clearJenkinsNS with new state ... a new Jenkins version
        doClearCall(t3State);
        assertLastClearBecause('Jenkins versions did not match. installVersion: 2.33.0, storedVersion: 2.32.1');

        // The the StorageNamespace should have been cleared (as expected).
        assertMagicValIsNotSet();
    });

    it('T4: 4rd run in browser - nothing changed from last time ... clear should not happen', () => {
        // Set the magic value again, call clear again with the same state as last time
        setMagicVal();
        doClearCall(t3State);
        // clear shouldn't have happened and the magic value should still be there.
        assertMagicValIsSet();
    });

    const t5State = {
        version: t3State.version,
        plugins: [
            { hpiPluginId: 'pluginA', hpiPluginVer: '1.0' },
            { hpiPluginId: 'pluginB', hpiPluginVer: '2.0' },
        ],
    };

    it('T5: 5th run in browser - plugin version change ... clear should happen', () => {
        // new state with a plugin version change
        doClearCall(t5State);
        // clear should have happened.
        assertLastClearBecause('Different plugin versions for plugin pluginB');
        assertMagicValIsNotSet();
    });

    it('T6: 6th run in browser - nothing changed ... clear should not happen', () => {
        setMagicVal();
        doClearCall(t5State);
        assertLastClearBecause('Different plugin versions for plugin pluginB');
        assertMagicValIsSet();
    });

    const t7State = {
        version: t3State.version,
        plugins: [
            { hpiPluginId: 'pluginA', hpiPluginVer: '1.0' },
            { hpiPluginId: 'pluginB', hpiPluginVer: '2.0' },
            { hpiPluginId: 'pluginC', hpiPluginVer: '1.0' },
        ],
    };

    it('T7: 7th run in browser - new plugin installed ... clear should happen', () => {
        // new state with a new plugin
        doClearCall(t7State);
        // clear should have happened.
        assertLastClearBecause('Different number of active plugins');
        assertMagicValIsNotSet();
    });

    it('T8: 8th run in browser - nothing changed ... clear should not happen', () => {
        setMagicVal();
        doClearCall(t7State);
        assertLastClearBecause('Different number of active plugins');
        assertMagicValIsSet();
    });

    const t9State = {
        version: t3State.version,
        plugins: [
            { hpiPluginId: 'pluginA', hpiPluginVer: '1.0' },
            { hpiPluginId: 'pluginB', hpiPluginVer: '2.0' },
            { hpiPluginId: 'pluginD', hpiPluginVer: '1.0' },
        ],
    };

    it('T9: 9th run in browser - new plugin installed and plugin removed... clear should happen', () => {
        // new state with a new plugin
        doClearCall(t9State);
        // clear should have happened.
        assertLastClearBecause('New plugin installed pluginD');
        assertMagicValIsNotSet();
    });

    it('T10: 10th run in browser - nothing changed ... clear should not happen', () => {
        setMagicVal();
        doClearCall(t9State);
        assertLastClearBecause('New plugin installed pluginD');
        assertMagicValIsSet();
    });
});
