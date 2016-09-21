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
            assert.isTrue(capable(testObj, 'a.b.LongName'));
        });

        it('matches on long name (using shortname)', () => {
            assert.isTrue(capable(testObj, 'LongName'));
        });

        it('fails to match', () => {
            assert.isFalse(capable(testObj, 'a.LongName'));
        });
    });

    describe('can - embedded function', () => {
        it('matches on long name (exact)', () => {
            assert.isTrue(testObj.can('a.b.LongName'));
        });

        it('matches on long name (using shortname)', () => {
            assert.isTrue(testObj.can('LongName'));
        });

        it('fails to match', () => {
            assert.isFalse(testObj.can('a.LongName'));
        });
    });
});
