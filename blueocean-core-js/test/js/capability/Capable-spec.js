/**
 * Created by cmeyers on 9/9/16.
 */
import { assert } from 'chai';

import { Capable } from '../../../src/js/capability/Capable';

describe('Capable', () => {
    let capable;

    beforeEach(() => {
        capable = new Capable();
        capable._capabilities = [
            'a.b.LongName',
            'ShortName',
        ];
    });

    describe('can', () => {
        it('matches on long name (exact)', () => {
            assert.isTrue(capable.can('a.b.LongName'));
        });

        it('matches on long name (using shortname)', () => {
            assert.isTrue(capable.can('LongName'));
        });

        it('fails to match', () => {
            assert.isFalse(capable.can('a.LongName'));
        });

        it('matches on long name using mixin', () => {
            const someObject = { _capabilities: ['a.b.LongName'] };
            someObject.can = capable.can;
            assert.isTrue(someObject.can('a.b.LongName'));
        });
    });
});
