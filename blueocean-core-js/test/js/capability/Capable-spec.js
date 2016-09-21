/**
 * Created by cmeyers on 9/9/16.
 */
import { assert } from 'chai';

import { Capable, capable } from '../../../src/js/capability/Capable';

describe('Capable', () => {
    let testObj;

    beforeEach(() => {
        testObj = {
            _capabilities: [
                'a.b.LongName',
                'ShortName',
            ],
        };
        testObj.can = new Capable().can;
    });

    describe('capable - static function', () => {
        it('matches on long name (exact)', () => {
            assert.isTrue(capable(testObj, 'a.b.LongName', 'a.b.OtherName'));
        });

        it('matches on long name (short version)', () => {
            assert.isTrue(capable(testObj, 'OtherName', 'LongName'));
        });

        it('matches with array containing long name (exact)', () => {
            assert.isTrue(capable(testObj, ['a.b.LongName', 'a.b.OtherName']));
        });

        it('matches with array containing long name (short version)', () => {
            assert.isTrue(capable(testObj, ['LongName', 'a.b.OtherName']));
        });

        it('fails to match', () => {
            assert.isFalse(capable(testObj, 'a.LongName'));
        });
    });

    describe('can - embedded function', () => {
        // don't need to test every scenario here; just that "embedding" the "can" func works
        it('matches on long name (exact)', () => {
            assert.isTrue(testObj.can('a.b.LongName'));
        });
    });
});
