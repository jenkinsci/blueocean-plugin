import { assert } from 'chai';
import React from 'react';

import {
    tokenizeANSIString,
    parseEscapeCode,
    makeReactChildren,
} from '../../main/js/util/ansi';

describe('ansi', () => {

    describe('react element generator', () => {

        describe('unstyled text', () => {
            it('will not wrap simple text input', () => {
                assert.deepEqual(makeReactChildren(['XXXXX']), ['XXXXX'], 'simple text 1');
                assert.deepEqual(makeReactChildren(['   XXXXX']), ['   XXXXX'], 'simple text 2');
                assert.deepEqual(makeReactChildren(['XXXXX   ']), ['XXXXX   '], 'simple text 3');
                assert.deepEqual(makeReactChildren(['']), [''], 'simple text 4');
                assert.deepEqual(makeReactChildren(['   ']), ['   '], 'simple text 5');
            });
        });

        describe('styled text', () => {

            function code(resetFG, resetBG, setFG, setBG) {
                return {
                    isSelectGraphicRendition: true,
                    resetFG,
                    resetBG,
                    setFG,
                    setBG,
                };
            }

            it('handles the simple case', () => {
                const input = [
                    code(false, false, 1, false),
                    'Test value',
                ];

                const result = makeReactChildren(input);

                assert.equal(result.length, 1, 'result length');

                const el = result[0];
                assert(React.isValidElement(el), 'is react element');
                assert.equal(el.type, 'span', 'element type');
                assert((el.props.className || '').indexOf('ansi-fg-1') !== -1, 'fg className');
                assert((el.props.className || '').indexOf('ansi-bg') == -1, 'no bg className');
                assert.equal(el.props.children, 'Test value', 'inner text');
            });

            it('will set bg and fg', () => {
                const input = [
                    code(false, false, 3, 5),
                    'Test value',
                ];

                const result = makeReactChildren(input);

                assert.equal(result.length, 1, 'result length');

                const el = result[0];
                assert(React.isValidElement(el), 'is react element');
                assert.equal(el.type, 'span', 'element type');
                assert((el.props.className || '').indexOf('ansi-fg-3') !== -1, 'fg className');
                assert((el.props.className || '').indexOf('ansi-bg-5') !== -1, 'bg className');
                assert.equal(el.props.children, 'Test value', 'inner text');
            });

            it('will set bg and fg then reset', () => {
                const input = [
                    code(false, false, 3, 5),
                    'Test value',
                    code(true, true, false, false),
                    'This is plain text',
                ];

                const result = makeReactChildren(input);

                assert.equal(result.length, 2, 'result length');

                let el = result[0];
                assert(React.isValidElement(el), 'is react element');
                assert.equal(el.type, 'span', 'element type');
                assert((el.props.className || '').indexOf('ansi-fg-3') !== -1, 'fg className');
                assert((el.props.className || '').indexOf('ansi-bg-5') !== -1, 'bg className');
                assert.equal(el.props.children, 'Test value', 'inner text');

                el = result[1];
                assert.equal(typeof el, 'string', 'typeof el');
                assert.equal(el, 'This is plain text', 'content');
            });

            it('will update just fg', () => {
                const input = [
                    code(false, false, 3, 5),
                    'Test value',
                    code(false, false, 5, false),
                    'This is colored text',
                ];

                const result = makeReactChildren(input);

                assert.equal(result.length, 2, 'result length');

                let el = result[0];
                assert(React.isValidElement(el), 'is react element');
                assert.equal(el.type, 'span', 'element type');
                assert((el.props.className || '').indexOf('ansi-fg-3') !== -1, 'fg className');
                assert((el.props.className || '').indexOf('ansi-bg-5') !== -1, 'bg className');
                assert.equal(el.props.children, 'Test value', 'inner text');

                el = result[1];
                assert(React.isValidElement(el), 'is react element');
                assert.equal(el.type, 'span', 'element type');
                assert((el.props.className || '').indexOf('ansi-fg-5') !== -1, 'fg className');
                assert((el.props.className || '').indexOf('ansi-bg-5') !== -1, 'bg className');
                assert.equal(el.props.children, 'This is colored text', 'inner text');
            });
        });
    });

    describe('escape code parser', () => {

        describe('with correct input', () => {

            function testCase(desc, input, expected) {
                it(desc, () => {
                    const result = parseEscapeCode(input);
                    assert.equal(result.isSelectGraphicRendition, true, 'result.isSelectGraphicRendition');
                    assert.equal(result.resetFG, expected.resetFG, 'result.resetFG');
                    assert.equal(result.resetBG, expected.resetBG, 'result.resetBG');
                    assert.equal(result.setFG, expected.setFG, 'result.setFG');
                    assert.equal(result.setBG, expected.setBG, 'result.setBG');
                })
            }

            testCase('supports test case 1', '\x1b[m', {
                resetFG: true,
                resetBG: true,
                setFG: false,
                setBG: false,
            });

            testCase('supports test case 2', '\x1b[31m', {
                resetFG: false,
                resetBG: false,
                setFG: 1,
                setBG: false,
            });

            testCase('supports test case 3', '\x1b[31;43m', {
                resetFG: false,
                resetBG: false,
                setFG: 1,
                setBG: 3,
            });

            testCase('supports test case 4', '\x1b[31;43;35m', {
                resetFG: false,
                resetBG: false,
                setFG: 5,
                setBG: 3,
            });

            testCase('supports test case 5', '\x1b[31;43;35;0m', {
                resetFG: true,
                resetBG: true,
                setFG: false,
                setBG: false,
            });

            testCase('supports test case 6', '\x1b[31;43;35;m', {
                resetFG: true,
                resetBG: true,
                setFG: false,
                setBG: false,
            });

            testCase('supports test case 7', '\x1b[31;43;35;;;m', {
                resetFG: true,
                resetBG: true,
                setFG: false,
                setBG: false,
            });

            testCase('supports test case 8', '\x1b[31;43;;36m', {
                resetFG: true,
                resetBG: true,
                setFG: 6,
                setBG: false,
            });
        });

        describe('with bad input', () => {

            function testCase(desc, input) {
                it(desc, () => {
                    const result = parseEscapeCode(input);
                    assert.equal(result.escapeCode, input, 'result.result');
                    assert.equal(result.isSelectGraphicRendition, false, 'result.isSelectGraphicRendition');
                })
            }

            testCase('skips bad input 1', '\x1bm');
            testCase('skips bad input 2', '\x1c[31m');
            testCase('skips bad input 3', 'x1b[31;43m');
            testCase('skips bad input 4', '[31;43;35m');
            testCase('skips bad input 5', '\x1b[31;43;35;0h');
            testCase('skips bad input 6', '\x1b[31;43;35;mz');
            testCase('skips bad input 7', '\x1b[31;43;a;35;;;m');
            testCase('skips bad input 8', '\x1b31;43;;36m');
        });
    });

    describe('tokenizer', () => {

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
                assert.equal(result[1].isSelectGraphicRendition, true, 'isSelectGraphicRendition');
                assert.equal(result[2], 'Right', 'second text part');
            });

            it('works when the escape code is at the start', () => {
                const result = tokenizeANSIString('\x1b[31mRed Text');

                assert.equal(result.length, 2, 'result size');
                assert.equal(typeof(result[0]), 'object', 'object for token');
                assert.equal(result[0].isSelectGraphicRendition, true, 'isSelectGraphicRendition');
                assert.equal(result[1], 'Red Text', 'text part');
            });

            it('works when the escape code is at the end', () => {
                const result = tokenizeANSIString('Schweppes\x1b[31m');

                assert.equal(result.length, 2, 'result size');
                assert.equal(result[0], 'Schweppes', 'text part');
                assert.equal(typeof(result[1]), 'object', 'object for token');
                assert.equal(result[1].isSelectGraphicRendition, true, 'isSelectGraphicRendition');
            });

            it('works when the text is only an escape code', () => {
                const result = tokenizeANSIString('\x1b[31m');

                assert.equal(result.length, 1, 'result size');
                assert.equal(typeof(result[0]), 'object', 'object for token');
                assert.equal(result[0].isSelectGraphicRendition, true, 'isSelectGraphicRendition');
            });

            it('works with multiple parameters', () => {
                const result = tokenizeANSIString('\x1b[31;43m');

                assert.equal(result.length, 1, 'result size');
                assert.equal(typeof(result[0]), 'object', 'object for token');
                assert.equal(result[0].isSelectGraphicRendition, true, 'isSelectGraphicRendition');
            });

            it('works when the text is multiple escape codes', () => {
                const result = tokenizeANSIString('\x1b[31m\x1b[42m');

                assert.equal(result.length, 2, 'result size');
                assert.equal(typeof(result[0]), 'object', 'object for token');
                assert.equal(result[0].isSelectGraphicRendition, true, 'isSelectGraphicRendition');
                assert.equal(typeof(result[1]), 'object', 'object for token');
                assert.equal(result[1].isSelectGraphicRendition, true, 'isSelectGraphicRendition');
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
})
;
