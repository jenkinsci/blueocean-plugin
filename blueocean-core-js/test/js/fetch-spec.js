import { assert } from 'chai';
import nock from 'nock';

import TestUtils from '../../src/js/testutils';
import { Fetch } from '../../src/js/fetch';


describe('Fetch', () => {
    describe('fetchJSON', () => {
        let nockServer = null;
        let requestUrl = null;

        const protocol = 'http';
        const host = 'example.com';
        const prefix = `${protocol}://${host}`;

        beforeEach(() => {
            TestUtils.patchFetchNoJWT();
            nockServer = nock(prefix);
            requestUrl = prefix;
        });

        describe('2xx success', () => {
            it('with simple object response body', () => {
                nockServer
                    .get('/success/simple')
                    .reply(200, { foo: 'bar' });

                requestUrl += '/success/simple';

                return Fetch.fetchJSON(requestUrl)
                    .then(
                        response => {
                            assert.isOk(response);
                            assert.equal(response.foo, 'bar');
                        }
                    );
            });

            it('with empty response body', () => {
                nockServer
                    .get('/success/empty')
                    .reply(200, null);

                requestUrl += '/success/empty';

                return Fetch.fetchJSON(requestUrl)
                    .then(
                        response => {
                            assert.isOk(response);
                        }
                    );
            });
        });

        describe('4xx failure', () => {
            it('with simple object response body', () => {
                nockServer
                    .get('/failure/simple')
                    .reply(400, { message: 'validation' });

                requestUrl += '/failure/simple';

                return Fetch.fetchJSON(requestUrl)
                    .then(
                        () => {
                            assert.fail(null, null, 'should not call success handler');
                        },
                        error => {
                            assert.isOk(error);
                            assert.isOk(error.responseBody);
                            assert.equal(error.responseBody.message, 'validation');
                        }
                    );
            });

            it('with empty response body', () => {
                nockServer
                    .get('/failure/empty')
                    .reply(400, null);

                requestUrl += '/failure/empty';

                return Fetch.fetchJSON(requestUrl)
                    .then(
                        () => {
                            assert.fail(null, null, 'should not call success handler');
                        },
                        error => {
                            assert.isOk(error);
                            assert.isNull(error.responseBody);
                        }
                    );
            });
        });

        // TODO: capabilities?
        // TODO: dedupe?
        // TODO: preloader
        // TODO: loading indicator

    });
});
