import { assert } from 'chai';

import apiUtils from '../../../../../main/js/creation/github/api/GithubApiUtils';

describe('GithubApiUtils', () => {
    describe('extractProtocolHost', () => {
        const expected = 'https://api.github.com';

        it('handles url with protocol and host only', () => {
            assert.equal(
                apiUtils.extractProtocolHost('https://api.github.com'),
                expected,
            );
        });
        it('handles url with path', () => {
            assert.equal(
                apiUtils.extractProtocolHost('https://api.github.com/api/v3/'),
                expected,
            );
        });
        it('handles url with query string', () => {
            assert.equal(
                apiUtils.extractProtocolHost('https://api.github.com?foo=bar&bar=foo'),
                expected,
            );
        });
        it('handles url with path and query string', () => {
            assert.equal(
                apiUtils.extractProtocolHost('https://api.github.com/api/v3/?foo=bar&bar=foo'),
                expected,
            );
        });
    });
});
