/**
 * Created by cmeyers on 9/8/16.
 */
import { assert } from 'chai';
import es6Promise from 'es6-promise'; es6Promise.polyfill();
import sinon from 'sinon';

import { CapabilityStore } from '../../../src/js/capability/CapabilityStore';
import { jenkinsNS } from '../../../src/js/storage';

const mockCapabilityApi = {
    fetchCapabilities: (classNames) => {
        const data = {
            map: {},
        };

        if (classNames.indexOf('A') !== -1) {
            data.map.A = { classes: ['A', 'A_one', 'A_two'] };
        }

        if (classNames.indexOf('B') !== -1) {
            data.map.B = { classes: ['B', 'B_one', 'B_two'] };
        }

        return new Promise(resolve => resolve(data));
    },
};

const fetchCapabilitiesSpy = sinon.spy(mockCapabilityApi, 'fetchCapabilities');

describe('CapabilityStore', () => {
    let capabilityStore;

    beforeEach(() => {
        capabilityStore = new CapabilityStore(mockCapabilityApi);
        fetchCapabilitiesSpy.reset();
        // Clear the localStorage namespace, making sure the test
        // REST API calls are not interfered with.
        jenkinsNS.clear();
    });

    it('resolves capabilities for a single class', (done) => {
        capabilityStore.resolveCapabilities('A')
            .then((capabilities) => {
                assert.equal(Object.keys(capabilities).length, 1);
                assert.isOk(capabilities.A);
                assert.equal(capabilities.A.length, 3);
                assert.equal(capabilities.A[0], 'A');
                assert.equal(capabilities.A[1], 'A_one');
                assert.equal(capabilities.A[2], 'A_two');
                done();
            });
    });

    it('resolves capabilities for multiple classes', (done) => {
        capabilityStore.resolveCapabilities('A', 'B')
            .then((capabilities) => {
                assert.equal(Object.keys(capabilities).length, 2);
                assert.isOk(capabilities.A);
                assert.equal(capabilities.A.length, 3);
                assert.isOk(capabilities.B);
                assert.equal(capabilities.B.length, 3);
                assert.equal(capabilities.B[0], 'B');
                assert.equal(capabilities.B[1], 'B_one');
                assert.equal(capabilities.B[2], 'B_two');
                done();
            });
    });

    it('only fetches from the API once when resolving the same class', (done) => {
        capabilityStore.resolveCapabilities('A')
            .then(() => {
                capabilityStore.resolveCapabilities('A')
                    .then(() => {
                        assert.isOk(fetchCapabilitiesSpy.calledOnce);
                        done();
                    });
            });
    });

    it('fetches from the API twice when resolving different classes', (done) => {
        capabilityStore.resolveCapabilities('A')
            .then(() => {
                capabilityStore.resolveCapabilities('B')
                    .then(() => {
                        assert.isOk(fetchCapabilitiesSpy.calledTwice);
                        done();
                    });
            });
    });
});
