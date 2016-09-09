/**
 * Created by cmeyers on 9/9/16.
 */
import { assert } from 'chai';
import sinon from 'sinon';

import testutils from '../../../src/js/testutils';

import { CapabilityApi } from '../../../src/js/capability/CapabilityApi';

const mock = {
    fetch: () => {},
    fetchJSON: () => {},
};
const fetch = sinon.spy(mock, 'fetch');
const fetchJSON = sinon.spy(mock, 'fetchJSON');

describe('CapabilityStore', () => {
    let capabilityApi;

    beforeEach(() => {
        testutils.patchFetch(fetchJSON, fetch);
        capabilityApi = new CapabilityApi();
    });

    afterEach(() => {
        testutils.restoreFetch();
    });

    describe('fetchCapabilities', () => {
        it('de-duplicates class names', () => {
            capabilityApi.fetchCapabilities('A', 'A', 'B');

            assert.isTrue(fetchJSON.calledOnce);
            const args = fetchJSON.args[0];
            const query = args[0].split('=').slice(-1).join('');
            assert.equal(query, 'A,B');
        });
    });
});
