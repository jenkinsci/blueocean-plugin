import { assert } from 'chai';

import {
    tokenizeANSIString,
} from '../../main/js/util/ansi';

describe('ansi', () => {

    describe('parser', () => {

        it('handles empty inputs', () => {
            assert.deepEqual(tokenizeANSIString(), [], 'No input');
            assert.deepEqual(tokenizeANSIString(''), [], 'Empty string');
            assert.deepEqual(tokenizeANSIString({}), [], 'Object');
        });

        it('is correct for strings without escapes', () => {
            assert.deepEqual(tokenizeANSIString('XXXXX'), ['XXXXX'], 'XXXXX');
            assert.deepEqual(tokenizeANSIString('A'), ['A'], 'A');
            assert.deepEqual(tokenizeANSIString('😍'), ['😍'], '😍');
            assert.deepEqual(tokenizeANSIString('Blue™'), ['Blue™'], 'Blue™');
            assert.deepEqual(tokenizeANSIString('Rubber baby buggy bumpers'), ['Rubber baby buggy bumpers'], 'Rubber baby buggy bumpers');
            assert.deepEqual(tokenizeANSIString('   XXXXX'), ['   XXXXX'], '   XXXXX');
            assert.deepEqual(tokenizeANSIString('XXXXX   '), ['XXXXX   '], 'XXXXX   ');
        });

        describe('basic input', () => {

            it('splits a string correctly', () => {
                const result = tokenizeANSIString('Left\x1b[31mRight');

                assert.equal(result.length, 3, 'result size');
                assert.equal(result[0], 'Left', 'first text part');
                assert.equal(typeof(result[1]), 'object', 'object for token');
                assert.equal(result[2], 'Right', 'second text part');
            });

            it('works when the escape code is at the start', () => {
                const result = tokenizeANSIString('\x1b[31mRed Text');

                assert.equal(result.length, 2, 'result size');
                assert.equal(typeof(result[0]), 'object', 'object for token');
                assert.equal(result[1], 'Red Text', 'text part');
            });

            it('works when the escape code is at the end', () => {
                const result = tokenizeANSIString('Schweppes\x1b[31m');

                assert.equal(result.length, 2, 'result size');
                assert.equal(result[0], 'Schweppes', 'text part');
                assert.equal(typeof(result[1]), 'object', 'object for token');
            });

            it('works when the text is only an escape code', () => {
                const result = tokenizeANSIString('\x1b[31m');

                assert.equal(result.length, 1, 'result size');
                assert.equal(typeof(result[0]), 'object', 'object for token');
            });

            it('works with multiple parameters', () => {
                const result = tokenizeANSIString('\x1b[31;43m');

                assert.equal(result.length, 1, 'result size');
                assert.equal(typeof(result[0]), 'object', 'object for token');
            });

            it('works when the text is multiple escape codes', () => {
                const result = tokenizeANSIString('\x1b[31m\x1b[42m');

                assert.equal(result.length, 2, 'result size');
                assert.equal(typeof(result[0]), 'object', 'object for token');
                assert.equal(typeof(result[1]), 'object', 'object for token');
            });

            it('survives malformed escapes', () => {
                const result1 = tokenizeANSIString('\x1b[32;1');
                const result2 = tokenizeANSIString('\x1b[32;');
                const result3 = tokenizeANSIString('\x1b[3');
                const result4 = tokenizeANSIString('\x1b[');
                const result5 = tokenizeANSIString('\x1b');

                assert.equal(result1.length, 1, 'result1 size');
                assert.equal(result2.length, 1, 'result2 size');
                assert.equal(result3.length, 1, 'result3 size');
                assert.equal(result4.length, 1, 'result4 size');
                assert.equal(result5.length, 1, 'result5 size');

                assert.equal(typeof(result1[0]), 'object', 'object for token (1)');
                assert.equal(typeof(result2[0]), 'object', 'object for token (2)');
                assert.equal(typeof(result3[0]), 'object', 'object for token (3)');
                assert.equal(typeof(result4[0]), 'object', 'object for token (4)');
                assert.equal(typeof(result5[0]), 'object', 'object for token (5)');
            });
        });

    });
});
