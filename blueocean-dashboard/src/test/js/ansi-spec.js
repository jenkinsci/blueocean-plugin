import { assert } from 'chai';

import {
    tokenizeANSIString,
} from '../../main/js/util/ansi';

describe('ansi', () => {

    describe('parser', () => {

        // it('handles empty inputs', () => {
        //     assert.deepEqual(tokenizeANSIString(), [], 'No input');
        //     assert.deepEqual(tokenizeANSIString(''), [], 'Empty string');
        //     assert.deepEqual(tokenizeANSIString({}), [], 'Object');
        // });

        it('is correct for strings without escapes', () => {
            assert.deepEqual(tokenizeANSIString('XXXXX'), ['XXXXX'], 'XXXXX');
            assert.deepEqual(tokenizeANSIString('A'), ['A'], 'A');
            assert.deepEqual(tokenizeANSIString('Rubber baby buggy bumpers'), ['Rubber baby buggy bumpers'], 'Rubber baby buggy bumpers');
            assert.deepEqual(tokenizeANSIString('XXXXX'), ['XXXXX'], 'XXXXX');
            assert.deepEqual(tokenizeANSIString('XXXXX'), ['XXXXX'], 'XXXXX');
        });

    });
});
