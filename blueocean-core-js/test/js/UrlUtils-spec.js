import { assert } from 'chai';

import * as UrlUtils from '../../src/js/utils/UrlUtils';

describe('UrlUtils', () => {
    describe('ensureTrailingSlash', () => {
        it('adds a slash when needed', () => {
            assert.equals(UrlUtils.ensureTrailingSlash('http://www.example.org'), 'http://www.example.org/', 'adds slash');
            assert.equals(UrlUtils.ensureTrailingSlash('x'), 'x/', 'adds slash');
        });

        it("doesn't add slash when not needed", () => {
            assert.equals(UrlUtils.ensureTrailingSlash('http://www.example.org/'), 'http://www.example.org/', "doesn't add slash");
            assert.equals(UrlUtils.ensureTrailingSlash(''), '', "doesn't add slash");
            assert.equals(UrlUtils.ensureTrailingSlash('/'), '/', "doesn't add slash");
        });
    });

    describe('doubleUriEncode', () => {
        it('encodes twice', () => {
            const gibberish = 'asdf^%$/\\xx';
            assert.equals(UrlUtils.doubleUriEncode(gibberish), encodeURIComponent(encodeURIComponent(gibberish)));
        });

        it('gives a blank string for null or undefined', () => {
            assert.equals(UrlUtils.doubleUriEncode(), '');
            assert.equals(UrlUtils.doubleUriEncode(null), '');
        });

        it("gives it a red hot go on other things that aren't strings", () => {
            assert.equals(UrlUtils.doubleUriEncode(false), 'false');
            assert.equals(UrlUtils.doubleUriEncode(true), 'true');
            assert.equals(UrlUtils.doubleUriEncode(0), '0');
            assert.equals(UrlUtils.doubleUriEncode(7), '7');
        });
    });
});
