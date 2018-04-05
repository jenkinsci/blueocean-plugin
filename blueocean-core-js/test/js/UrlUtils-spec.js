import { assert } from 'chai';

import * as UrlUtils from '../../src/js/utils/UrlUtils';

describe('UrlUtils', () => {
    describe('ensureTrailingSlash', () => {
        it('adds a slash when needed', () => {
            assert.equal(UrlUtils.ensureTrailingSlash('http://www.example.org'), 'http://www.example.org/', 'adds slash');
            assert.equal(UrlUtils.ensureTrailingSlash('x'), 'x/', 'adds slash');
        });

        it("doesn't add slash when not needed", () => {
            assert.equal(UrlUtils.ensureTrailingSlash('http://www.example.org/'), 'http://www.example.org/', "doesn't add slash");
            assert.equal(UrlUtils.ensureTrailingSlash(''), '', "doesn't add slash");
            assert.equal(UrlUtils.ensureTrailingSlash('/'), '/', "doesn't add slash");
        });
    });

    describe('doubleUriEncode', () => {
        it('encodes twice', () => {
            const gibberish = 'asdf^%$/\\xx';
            assert.equal(UrlUtils.doubleUriEncode(gibberish), encodeURIComponent(encodeURIComponent(gibberish)));
        });

        it('gives a blank string for null or undefined', () => {
            assert.equal(UrlUtils.doubleUriEncode(), '');
            assert.equal(UrlUtils.doubleUriEncode(null), '');
        });

        it("gives it a red hot go on other things that aren't strings", () => {
            assert.equal(UrlUtils.doubleUriEncode(false), 'false');
            assert.equal(UrlUtils.doubleUriEncode(true), 'true');
            assert.equal(UrlUtils.doubleUriEncode(0), '0');
            assert.equal(UrlUtils.doubleUriEncode(7), '7');
        });
    });
});
