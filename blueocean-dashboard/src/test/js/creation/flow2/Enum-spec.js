import { assert } from 'chai';

import { Enum } from '../../../../main/js/creation/flow2/Enum';

describe('Enum', () => {
    describe('construction', () => {
        it('support "array" form', () => {
            const Suit = new Enum([
                'SPADES', 'CLUBS', 'DIAMONDS', 'HEARTS',
            ]);

            assert.equal(Suit.SPADES, 'SPADES');
            assert.equal(Suit.CLUBS, 'CLUBS');
            assert.equal(Suit.DIAMONDS, 'DIAMONDS');
            assert.equal(Suit.HEARTS, 'HEARTS');
        });
        it('support "object" form', () => {
            const Suit = new Enum({
                SPADES: 'spades',
                CLUBS: 'clubs',
                DIAMONDS: 'diamonds',
                HEARTS: 'hearts',
            });

            assert.equal(Suit.SPADES, 'SPADES');
            assert.equal(Suit.CLUBS, 'CLUBS');
            assert.equal(Suit.DIAMONDS, 'DIAMONDS');
            assert.equal(Suit.HEARTS, 'HEARTS');
        });
    });
    describe('extend', () => {
        it('should allow additional keys to be added', () => {
            const alpha1 = new Enum({ A: 'a', B: 'b', C: 'c' });
            const alpha2 = alpha1.extend({ X: 'x', Y: 'y', Z: 'z' });

            assert.equal(alpha1.values().length, 3);
            assert.equal(alpha2.values().length, 6);
        });
    });
    describe('values', () => {
        it('should return an array with all values', () => {
            const Suit = new Enum({
                SPADES: 'spades',
                CLUBS: 'clubs',
                DIAMONDS: 'diamonds',
                HEARTS: 'hearts',
            });

            assert.equal(Suit.values().length, 4);
            assert.equal(Suit.values()[0], 'SPADES');
            assert.equal(Suit.values()[1], 'CLUBS');
            assert.equal(Suit.values()[2], 'DIAMONDS');
            assert.equal(Suit.values()[3], 'HEARTS');
        });
    });
});
